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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.database.IslndDb;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.fragments.FeedFragment;
import io.islnd.android.islnd.app.fragments.ViewFriendsFragment;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;

import io.islnd.android.islnd.messaging.MessageLayer;

public class NavBaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = NavBaseActivity.class.getSimpleName();

    private final static int REQUEST_SMS = 0;
    private final static int REQUEST_CONTACT = 1;

    private final static int CONTACT_RESULT = 0;

    private DrawerLayout mDrawerLayout;
    private EditText mSmsEditText = null;
    private View mDialogView = null;
    private Context mContext;
    private ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        mContext = this;
        onCreateDrawer();

        // Set launching fragment
        Fragment fragment = new FeedFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // Create sync account and force sync
        Account account = createSyncAccount(this);
        mResolver = getContentResolver();
        mResolver.setSyncAutomatically(
                account,
                getString(R.string.content_authority),
                true);
        mResolver.requestSync(account, IslndContract.CONTENT_AUTHORITY, new Bundle());
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
        ImageView navProfileImage = (ImageView) header.findViewById(R.id.nav_profile_image);
        ImageView navHeaderImage = (ImageView) header.findViewById(R.id.nav_header_image);
        TextView navUserName = (TextView) header.findViewById(R.id.nav_user_name);
        String myDisplayName = Util.getDisplayName(mContext);

        navProfileImage.setOnClickListener(
                (View v) -> {
                    Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                    profileIntent.putExtra(ProfileActivity.USER_ID_EXTRA, myDisplayName);
                    startActivity(profileIntent);
                });
        navUserName.setText(myDisplayName);

        int myUserId = Util.getUserId(mContext);
        if (myUserId >= 0) {
            Profile profile = DataUtils.getProfile(getApplicationContext(), myUserId);
            Uri profileImageUri = profile.getProfileImageUri();
            Uri headerImageUri = profile.getHeaderImageUri();
            ImageUtil.setNavProfileImageSampled(getApplicationContext(), navProfileImage,
                    profileImageUri);
            ImageUtil.setNavHeaderImageSampled(getApplicationContext(), navHeaderImage,
                    headerImageUri);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        boolean isFragment = false;

        switch (item.getItemId()) {
            case R.id.nav_feed:
                fragment = new FeedFragment();
                isFragment = true;
                break;
            case R.id.nav_profile:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                profileIntent.putExtra(ProfileActivity.USER_ID_EXTRA, Util.getUserId(mContext));
                startActivity(profileIntent);
                break;
            case R.id.nav_friends:
                fragment = new ViewFriendsFragment();
                isFragment = true;
                break;
            case R.id.nav_add_friend:
                addFriendActionDialog();
                break;
            case R.id.nav_settings:
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
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
            return;
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
            case REQUEST_SMS: {
                // Permission granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    smsAllowDialog();
                }
                return;
            }
            case REQUEST_CONTACT: {
                // Permission granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importContact(null);
                }
                return;
            }
        }
    }

    private void addFriendActionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_friend_dialog)
                .setItems(R.array.nav_add_friend_actions, (DialogInterface dialog, int which) -> {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.qr_dialog, null);

        Button getQrButton = (Button) dialogView.findViewById(R.id.get_qr_button);
        getQrButton.setOnClickListener((View v) -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setCaptureActivity(VerticalCaptureActivity.class);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        });

        ImageView qrImageView = (ImageView) dialogView.findViewById(R.id.qr_image_view);
        Util.buildQrCode(qrImageView,
                MessageLayer.getEncodedIdentityString(getApplicationContext()));

        builder.setView(dialogView)
                .setTitle(getString(R.string.qr_dialog_title))
                .show();
    }

    private void smsAllowDialog() {
        // Check permission if we have to.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestSmsPermissions();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
            ActivityCompat.requestPermissions(this,
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
            ActivityCompat.requestPermissions(this,
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

        startActivityForResult(new Intent(
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
                    DataUtils.deleteAll(this);
                    IslndDb.createIdentity(getApplicationContext(), editText.getText().toString());
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

    public static Account createSyncAccount(Context context) {
        Account newAccount = new Account(
                context.getString(R.string.sync_account),
                context.getString(R.string.sync_account_type));

        AccountManager accountManager = (AccountManager) context.getSystemService(context.ACCOUNT_SERVICE);
        accountManager.addAccountExplicitly(newAccount, null, null);

        return newAccount;
    }
}
