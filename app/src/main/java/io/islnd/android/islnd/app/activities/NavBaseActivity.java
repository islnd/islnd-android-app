package io.islnd.android.islnd.app.activities;

import android.Manifest;
import android.content.ContentResolver;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import io.islnd.android.islnd.app.NotificationHelper;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.fragments.FeedFragment;
import io.islnd.android.islnd.app.fragments.NotificationsFragment;
import io.islnd.android.islnd.app.fragments.ShowQrFragment;
import io.islnd.android.islnd.app.fragments.ViewFriendsFragment;
import io.islnd.android.islnd.app.loader.LoaderId;
import io.islnd.android.islnd.app.preferences.SettingsActivity;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.MessageLayer;
import io.islnd.android.islnd.messaging.ServerTime;

public class NavBaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = NavBaseActivity.class.getSimpleName();

    public static final String NOTIFICATION_ACTION = "NOTIFICATION_ACTION";

    private static final String FRAGMENT_STATE = "FRAGMENT_STATE";

    private static final int REQUEST_SMS = 0;
    private static final int REQUEST_CONTACT = 1;

    private static final int CONTACT_RESULT = 0;

    private DrawerLayout mDrawerLayout;
    private EditText mSmsEditText = null;
    private View mDialogView = null;

    private ImageView mNavProfileImage;
    private ImageView mNavHeaderImage;
    private TextView mNavUserName;
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        onCreateDrawer();
        
        ServerTime.synchronize(this, false);

        Intent intent = getIntent();

        // Set fragment
        if (savedInstanceState != null) {
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_STATE);
        } else if (NOTIFICATION_ACTION.equals(intent.getAction())) {
            NotificationHelper.cancelNotifications(this);
            mFragment = new NotificationsFragment();
        } else {
            mFragment = new FeedFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mFragment)
                .commit();

        getSupportLoaderManager().initLoader(LoaderId.NAV_BASE_ACTIVITY_LOADER_ID, new Bundle(), this);
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
            mFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //Fragment fragment = null;
        boolean isFragment = false;

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.content_frame);

        switch (item.getItemId()) {
            case R.id.nav_feed:
                if (currentFragment instanceof FeedFragment) {
                    break;
                }
                mFragment = new FeedFragment();
                isFragment = true;
                break;
            case R.id.nav_profile:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                profileIntent.putExtra(ProfileActivity.USER_ID_EXTRA, Util.getUserId(this));
                startActivity(profileIntent);
                break;
            case R.id.nav_friends:
                if (currentFragment instanceof ViewFriendsFragment) {
                    break;
                }
                mFragment = new ViewFriendsFragment();
                isFragment = true;
                break;
            case R.id.nav_notifications:
                if (currentFragment instanceof NotificationsFragment) {
                    break;
                }
                mFragment = new NotificationsFragment();
                isFragment = true;
                break;
            case R.id.nav_add_friend:
                addFriendActionDialog();
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        if (isFragment) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, mFragment)
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
                boolean friendAdded = MessageLayer.addFriendFromEncodedIdentityString(
                        getApplicationContext(),
                        contents);
                String message = friendAdded
                        ? getString(R.string.added_new_friend_message)
                        : getString(R.string.already_friends_message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        getSupportFragmentManager().putFragment(savedInstanceState, FRAGMENT_STATE, mFragment);
        super.onSaveInstanceState(savedInstanceState);
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
        mFragment = new ShowQrFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mFragment)
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
                MessageLayer.getMyIdentity(getApplicationContext());

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
        ContentResolver cr = getContentResolver();
        Cursor cursorID = cr.query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {
            String contactId =
                    cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));

            Cursor cursorPhone = cr.query(
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

    public void notificationBadgeClick() {
        Log.v(TAG, "notificationBadgeClick");

        NotificationHelper.cancelNotifications(getApplicationContext());

        mFragment = new NotificationsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mFragment)
                .addToBackStack("")
                .commit();
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
