package io.islnd.android.islnd.app.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.IslndDb;
import io.islnd.android.islnd.app.fragments.FeedFragment;
import io.islnd.android.islnd.app.fragments.ShowQrFragment;
import io.islnd.android.islnd.app.fragments.ViewFriendsFragment;
import io.islnd.android.islnd.app.preferences.SettingsActivity;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;

import io.islnd.android.islnd.messaging.MessageLayer;
import io.islnd.android.islnd.app.EventPushService;
import io.islnd.android.islnd.messaging.event.Event;
import io.islnd.android.islnd.messaging.event.EventListBuilder;
import io.islnd.android.islnd.messaging.event.EventProcessor;

public class NavBaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private final static String TAG = NavBaseActivity.class.getSimpleName();

    private final static int REQUEST_SMS = 0;
    private final static int REQUEST_CONTACT = 1;

    private final static int CONTACT_RESULT = 0;

    private DrawerLayout mDrawerLayout;
    private EditText mSmsEditText = null;
    private View mDialogView = null;
    private ContentResolver mResolver;
    private Account mSyncAccount;

    private ImageView mNavProfileImage;
    private ImageView mNavHeaderImage;
    private TextView mNavUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.applyAppTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        onCreateDrawer();

        // Set launching fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new FeedFragment())
                .commit();

        // Create sync account and force sync
        mSyncAccount = createAndRegisterSyncAccount(this);
        mResolver = getContentResolver();
        mResolver.setSyncAutomatically(
                mSyncAccount,
                getString(R.string.content_authority),
                true);
        mResolver.requestSync(mSyncAccount, IslndContract.CONTENT_AUTHORITY, new Bundle());

        getSupportLoaderManager().initLoader(0, new Bundle(), this);
    }

    private void onCreateDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set user in nav drawer
        View header = navigationView.getHeaderView(0);
        mNavProfileImage = (ImageView) header.findViewById(R.id.nav_profile_image);
        mNavHeaderImage = (ImageView) header.findViewById(R.id.nav_header_image);
        mNavUserName = (TextView) header.findViewById(R.id.nav_user_name);

        int myUserId = Util.getUserId(this);
        mNavProfileImage.setOnClickListener(
                (View v) -> {
                    Intent profileIntent = new Intent(this, ProfileActivity.class);
                    profileIntent.putExtra(ProfileActivity.USER_ID_EXTRA, myUserId);
                    startActivity(profileIntent);
                });
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        boolean isFragment = false;

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.content_frame);

        switch (item.getItemId()) {
            case R.id.nav_feed:
                if(currentFragment instanceof FeedFragment) {
                    break;
                }
                fragment = new FeedFragment();
                isFragment = true;
                break;
            case R.id.nav_profile:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                profileIntent.putExtra(ProfileActivity.USER_ID_EXTRA, Util.getUserId(this));
                startActivity(profileIntent);
                break;
            case R.id.nav_friends:
                if(currentFragment instanceof ViewFriendsFragment) {
                    break;
                }
                fragment = new ViewFriendsFragment();
                isFragment = true;
                break;
            case R.id.nav_add_friend:
                addFriendActionDialog();
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.delete_database:
                DataUtils.deleteAll(this);
                break;
            case R.id.edit_username:
                editUsernameDialog();
                break;
            case R.id.edit_api_key:
                editApiKey();
                break;
        }

        if (isFragment) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack("")
                    .commit();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d(TAG, "Cancelled scan");
            } else {
                String contents = result.getContents();
                Log.d(TAG, "Contents: " + contents);
                MessageLayer.addFriendFromEncodedIdentityString(getApplicationContext(), contents);
            }
        // Not QR result
        } else {
            if (requestCode == CONTACT_RESULT && resultCode == RESULT_OK) {
                Uri uriContact = data.getData();

                String number = retrieveContactNumber(uriContact);
                if (mSmsEditText != null) {
                    mSmsEditText.setText(number);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SMS:
                // Permission granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    smsAllowDialog();
                }
                break;
            case REQUEST_CONTACT:
                // Permission granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importContact(null);
                }
                break;
        }
    }

    private void addFriendActionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(R.string.add_friend_dialog)
                .setItems(
                        R.array.nav_add_friend_actions,
                        (DialogInterface dialog, int which) -> {
                            switch (which) {
                                case 0: // QR
                                    qrCodeActionDialog();
                                    break;
                                case 1: // SMS
                                    smsAllowDialog();
                                    break;
                            }
                        })
                .show();
    }

    private void qrCodeActionDialog() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new ShowQrFragment())
                .addToBackStack("")
                .commit();
    }

    private void smsAllowDialog() {
        // Check permission if we have to.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestSmsPermissions();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        mDialogView = getLayoutInflater().inflate(R.layout.sms_allow_dialog, null);
        builder.setView(mDialogView);

        mSmsEditText = (EditText) mDialogView.findViewById(R.id.sms_number_edit_text);

        builder.setPositiveButton(
                getString(R.string.send), (DialogInterface dialog, int id) -> {
                    sendSms(mSmsEditText.getText().toString());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void sendSms(String number) {
        String sms = getString(R.string.sms_prefix) +
                MessageLayer.getEncodedIdentityString(getApplicationContext());

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, sms, null, null);
            Snackbar.make(mDrawerLayout, "SMS sent!", Snackbar.LENGTH_LONG).show();
            Log.d(TAG, "SMS sent!");
        } catch (Exception e) {
            Snackbar.make(mDrawerLayout, "SMS failed!", Snackbar.LENGTH_LONG).show();
            Log.d(TAG, "SMS failed!");
            e.printStackTrace();
        }
    }

    private void requestSmsPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.SEND_SMS)) {
            // Show an explanation to the user
            Snackbar.make(mDrawerLayout, R.string.request_sms_send_explanation,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, (View view) -> {
                        ActivityCompat.requestPermissions(NavBaseActivity.this,
                                new String[]{Manifest.permission.SEND_SMS},
                                REQUEST_SMS);
                    })
                    .show();
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SMS);
        }
    }

    private void requestContactPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {
            // Show an explanation to the user
            Snackbar.make(mDialogView, R.string.request_contact_explanation,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, (View view) -> {
                        ActivityCompat.requestPermissions(NavBaseActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                REQUEST_CONTACT);
                    })
                    .show();
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_CONTACT);
        }
    }

    public void importContact(View view) {
        // Check permission if we have to.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestContactPermissions();
            return;
        }

        startActivityForResult(
                new Intent(
                        Intent.ACTION_PICK,
                        ContactsContract.Contacts.CONTENT_URI), CONTACT_RESULT);
    }

    private String retrieveContactNumber(Uri uriContact) {
        String contactNumber = null;

        // getting contacts ID
        ContentResolver cr = mResolver;
        Cursor cursorID = cr.query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {
            String contactId =
                    cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));

            Cursor cursorPhone = mResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                            ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                    new String[]{contactId},
                    null);

            if (cursorPhone.moveToFirst()) {
                contactNumber =
                        cursorPhone.getString(
                                cursorPhone.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            cursorPhone.close();
        }

        cursorID.close();
        return contactNumber;
    }

    private void editUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mDialogView = getLayoutInflater().inflate(R.layout.edit_username_dialog, null);
        builder.setView(mDialogView);

        EditText editText = (EditText) mDialogView.findViewById(R.id.edit_username_edit_text);

        builder.setPositiveButton(getString(android.R.string.ok),
                (DialogInterface dialog, int id) -> {
                    final String newDisplayName = editText.getText().toString();
                    if (Util.getUserId(this) < 0) { //--create user for this device
                        IslndDb.createIdentity(
                                getApplicationContext(),
                                newDisplayName);
                    } else { //--only update display name
                        List<Event> eventList = new EventListBuilder(this)
                                .changeDisplayName(newDisplayName)
                                .build();
                        for (Event event : eventList) {
                            EventProcessor.process(this, event);
                            Intent pushEventService = new Intent(this, EventPushService.class);
                            pushEventService.putExtra(EventPushService.EVENT_EXTRA, event);
                            startService(pushEventService);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void editApiKey() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mDialogView = getLayoutInflater().inflate(R.layout.edit_api_key_dialog, null);
        builder.setView(mDialogView);

        EditText editText = (EditText) mDialogView.findViewById(R.id.edit_api_key_edit_text);

        builder.setPositiveButton(getString(android.R.string.ok),
                (DialogInterface dialog, int id) -> {
                    Util.setApiKey(getApplicationContext(), editText.getText().toString());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static Account createAndRegisterSyncAccount(Context context) {
        Account newAccount = Util.getSyncAccount(context);

        //--Register account
        AccountManager accountManager = (AccountManager) context.getSystemService(context.ACCOUNT_SERVICE);
        accountManager.addAccountExplicitly(newAccount, null, null);

        return newAccount;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                IslndContract.ProfileEntry.TABLE_NAME + "." + IslndContract.PostEntry._ID,
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI,
                IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI,
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME
        };

        return new CursorLoader(
                this,
                IslndContract.ProfileEntry.buildProfileUriWithUserId(IslndContract.UserEntry.MY_USER_ID),
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        ImageUtil.setNavProfileImageSampled(
                this,
                mNavProfileImage,
                Uri.parse(data.getString(data.getColumnIndex(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI))));
        ImageUtil.setNavProfileImageSampled(
                this,
                mNavHeaderImage,
                Uri.parse(data.getString(data.getColumnIndex(IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI))));
        mNavUserName.setText(
                data.getString(data.getColumnIndex(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME))
        );
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
