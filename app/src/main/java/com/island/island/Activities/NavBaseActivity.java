package com.island.island.Activities;

import android.Manifest;
import android.app.AlertDialog;
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
import android.support.v4.app.DialogFragment;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.island.island.Database.FriendDatabase;
import com.island.island.Database.IslandDB;
import com.island.island.Database.PostDatabase;
import com.island.island.Database.ProfileDatabase;
import com.island.island.DeletePostFragment;
import com.island.island.Models.PostKey;
import com.island.island.R;
import com.island.island.Utils.ImageUtils;
import com.island.island.Utils.Utils;

import org.island.messaging.MessageLayer;

public class NavBaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DeletePostFragment.NoticeDeletePostListener  {
    private final static String TAG = NavBaseActivity.class.getSimpleName();

    private final static int REQUEST_SMS = 0;
    private final static int REQUEST_CONTACT = 1;

    private final static int CONTACT_RESULT = 0;

    private DrawerLayout mDrawerLayout;
    private EditText mSmsEditText = null;
    private View mDialogView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        onCreateDrawer();

        // Set launching fragment
        Fragment fragment = new FeedActivity();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
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
        String userName = Utils.getUser(NavBaseActivity.this);

        navProfileImage.setOnClickListener((View v) -> {
                    Intent profileIntent = new Intent(NavBaseActivity.this, ProfileActivity.class);
                    profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, userName);
                    startActivity(profileIntent);
                });
        navUserName.setText(userName);

        ProfileDatabase profileDatabase = ProfileDatabase.getInstance(getApplicationContext());
        Uri profileImageUri = Uri.parse(profileDatabase.getProfileImageUri(userName));
        Uri headerImageUri = Uri.parse(profileDatabase.getHeaderImageUri(userName));
        ImageUtils.setNavProfileImageSampled(getApplicationContext(), navProfileImage,
                profileImageUri);
        ImageUtils.setNavHeaderImageSampled(getApplicationContext(), navHeaderImage,
                headerImageUri);
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
                fragment = new FeedActivity();
                isFragment = true;
                break;
            case R.id.nav_profile:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, Utils.getUser(this));
                startActivity(profileIntent);
                break;
            case R.id.nav_friends:
                fragment = new ViewFriendsActivity();
                isFragment = true;
                break;
            case R.id.nav_add_friend:
                addFriendActionDialog();
                break;
            case R.id.nav_settings:
                break;
            case R.id.delete_database:
                FriendDatabase.getInstance(this).deleteAll();
                ProfileDatabase.getInstance(this).deleteAll();
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

    @Override
    public void onDeletePostDialogPositiveClick(DialogFragment dialogFragment) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment instanceof FeedActivity) {
            ((FeedActivity) fragment).onDeletePostDialogPositiveClick(dialogFragment);
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
        builder.setTitle(R.string.qr_action_dialog)
                .setItems(R.array.qr_actions, (DialogInterface dialog, int which) -> {
                    switch (which) {
                        case 0: // Show QR
                            startActivity(new Intent(this, ShowQRActivity.class));
                            break;
                        case 1: // Get QR
                            IntentIntegrator integrator = new IntentIntegrator(this);
                            integrator.setCaptureActivity(VerticalCaptureActivity.class);
                            integrator.setOrientationLocked(false);
                            integrator.initiateScan();
                            break;
                    }
                })
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

        builder.setPositiveButton(getString(R.string.send), (DialogInterface dialog, int id) -> {
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
        ContentResolver cr = getContentResolver();
        Cursor cursorID = cr.query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {
            String contactId =
                    cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));

            Cursor cursorPhone = getContentResolver().query(
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
                    FriendDatabase.getInstance(getApplicationContext()).deleteAll();
                    ProfileDatabase.getInstance(getApplicationContext()).deleteAll();
                    PostDatabase.getInstance(getApplicationContext()).deleteAll();
                    IslandDB.createIdentity(getApplicationContext(), editText.getText().toString());
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
                    Utils.setApiKey(getApplicationContext(), editText.getText().toString());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
