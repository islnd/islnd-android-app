package com.island.island.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.island.island.Adapters.PostAdapter;
import com.island.island.Database.FriendDatabase;
import com.island.island.Database.ProfileDatabase;
import com.island.island.Models.Post;
import com.island.island.Database.IslandDB;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;
import com.island.island.Utils.Utils;

import org.island.messaging.MessageLayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeedActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private static final String TAG = FeedActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Post> mArrayOfPosts;
    private Set<String> mPostMap;
    private SwipeRefreshLayout refreshLayout;
    private CoordinatorLayout mainLayout;

    private EditText smsEditText = null;
    private View dialogView = null;

    private final static int REQUEST_SMS = 0;
    private final static int REQUEST_CONTACT = 1;

    private final static int CONTACT_RESULT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Boilerplate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mainLayout = (CoordinatorLayout) findViewById(R.id.feed_main_layout);

        // TODO: Remove after login implemented
        // Set user hack
        String USER = "julio";
        IslandDB.createIdentity(this, USER);

        // Nav drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set user in nav drawer
        View header = navigationView.getHeaderView(0);
        ImageView navProfileImage = (ImageView) header.findViewById(R.id.nav_profile_image);
        TextView navUserName = (TextView) header.findViewById(R.id.nav_user_name);
        final String userName = Utils.getUser(FeedActivity.this);

        navProfileImage.setOnClickListener(
                (View v) ->
                {
                    Intent profileIntent = new Intent(FeedActivity.this, ProfileActivity.class);
                    profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, userName);
                    startActivity(profileIntent);
                });
        navUserName.setText(userName);

        // Feed posts setup
        mArrayOfPosts = new ArrayList<>();
        mPostMap = new HashSet<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.feed_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PostAdapter(this, mArrayOfPosts);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        // Populate feed
        new GetPostsTask().execute();

        // Swipe to refresh
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);

        refreshLayout.setOnRefreshListener(() ->
        {
            // TODO: This will add duplicates, okay for now
            new GetPostsTask().execute();
        });
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.nav_profile:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, Utils.getUser(this));
                startActivity(profileIntent);
                break;
            case R.id.nav_friends:
                startActivity(new Intent(this, ViewFriendsActivity.class));
                break;
            case R.id.nav_settings:
                break;
            case R.id.qr_code:
                qrCodeActionDialog();
                break;
            case R.id.delete_database:
                FriendDatabase.getInstance(this).deleteAll();
                ProfileDatabase.getInstance(this).deleteAll();
                break;
            case R.id.sms_allow_user:
                smsAllowDialog();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
        {
            if(result.getContents() == null)
            {
                Log.d(TAG, "Cancelled scan");
            }
            else
            {
                String contents = result.getContents();
                Log.d(TAG, "Contents: " + contents);
                MessageLayer.addFriendFromEncodedIdentityString(getApplicationContext(), contents);
            }
        }
        // Not QR result
        else
        {
            if (requestCode == CONTACT_RESULT && resultCode == RESULT_OK)
            {
                Uri uriContact = data.getData();

                String number = retrieveContactNumber(uriContact);
                if (smsEditText != null)
                {
                    smsEditText.setText(number);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_SMS:
            {
                // Permission granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    smsAllowDialog();
                }
                return;
            }
            case REQUEST_CONTACT:
            {
                // Permission granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    importContact(null);
                }
                return;
            }
        }
    }

    public void startNewPostActivity(View view)
    {
        Intent newPostIntent = new Intent(FeedActivity.this, NewPostActivity.class);
        startActivity(newPostIntent);
    }

    private class GetPostsTask extends AsyncTask<Void, Void, List<Post>>
    {
        private final String TAG = GetPostsTask.class.getSimpleName();

        @Override
        protected List<Post> doInBackground(Void... params) {
            return MessageLayer.getPosts(getApplicationContext());
        }

        @Override
        protected void onPostExecute(List<Post> posts) {
            boolean adapterChanged = false;

            if (posts != null) {
                for (Post p : posts) {
                    Log.v(TAG, "looking at post with key " + p.getKey());
                    if (!mPostMap.contains(p.getKey())) {
                        mPostMap.add(p.getKey());

                        //--TODO extract and use binary search
                        int insertionPoint = 0;
                        while (insertionPoint < mArrayOfPosts.size()
                                && p.getTimestamp() < mArrayOfPosts.get(insertionPoint).getTimestamp()) {
                            Log.v(TAG, "" + insertionPoint);
                            insertionPoint++;
                        }

                        mArrayOfPosts.add(insertionPoint, p);
                        adapterChanged = true;
                    }
                }
            }

            if (adapterChanged) {
                mAdapter.notifyDataSetChanged();
            }

            refreshLayout.setRefreshing(false);
        }
    }

    private void qrCodeActionDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.qr_action_dialog)
                .setItems(R.array.qr_actions, (DialogInterface dialog, int which) ->
                {
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

    private void smsAllowDialog()
    {
        // Check permission if we have to.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestSmsPermissions();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialogView = getLayoutInflater().inflate(R.layout.sms_allow_dialog, null);
        builder.setView(dialogView);

        smsEditText = (EditText) dialogView.findViewById(R.id.sms_number_edit_text);

        builder.setPositiveButton(getString(R.string.send), (DialogInterface dialog, int id) ->
        {
            sendSms(smsEditText.getText().toString());
        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void sendSms(String number)
    {
        String sms = getString(R.string.sms_prefix) +
                MessageLayer.getEncodedIdentityString(getApplicationContext());

        try
        {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, sms, null, null);
            Snackbar.make(mainLayout, "SMS sent!", Snackbar.LENGTH_LONG).show();
            Log.d(TAG, "SMS sent!");
        }
        catch (Exception e)
        {
            Snackbar.make(mainLayout, "SMS failed!", Snackbar.LENGTH_LONG).show();
            Log.d(TAG, "SMS failed!");
            e.printStackTrace();
        }
    }

    private void requestSmsPermissions()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.SEND_SMS))
        {
            // Show an explanation to the user
            Snackbar.make(mainLayout, R.string.request_sms_send_explanation,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, (View view) ->
                    {
                            ActivityCompat.requestPermissions(FeedActivity.this,
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

    private void requestContactPermissions()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS))
        {
            // Show an explanation to the user
            Snackbar.make(dialogView, R.string.request_contact_explanation,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, (View view) ->
                    {
                        ActivityCompat.requestPermissions(FeedActivity.this,
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

    public void importContact(View view)
    {
        // Check permission if we have to.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestContactPermissions();
            return;
        }

        startActivityForResult(new Intent(
                        Intent.ACTION_PICK,
                        ContactsContract.Contacts.CONTENT_URI), CONTACT_RESULT);
    }

    private String retrieveContactNumber(Uri uriContact)
    {
        String contactNumber = null;

        // getting contacts ID
        ContentResolver cr = getContentResolver();
        Cursor cursorID = cr.query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst())
        {
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

            if (cursorPhone.moveToFirst())
            {
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
}
