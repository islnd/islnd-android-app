package com.island.island.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.island.island.Adapters.PostAdapter;
import com.island.island.Models.Post;
import com.island.island.Models.User;
import com.island.island.Database.IslandDB;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;
import com.island.island.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout refreshLayout;

    private static String TAG = "FeedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Boilerplate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO: Remove after login implemented
        // Set user hack
        Utils.setUser(this, "Thom Yorke");

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

        navProfileImage.setOnClickListener((View v) ->
        {
            Intent profileIntent = new Intent(FeedActivity.this, ProfileActivity.class);
            profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, userName);
            startActivity(profileIntent);
        });
        navUserName.setText(userName);

        // Feed posts setup
        List<Post> arrayOfPosts = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.feed_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PostAdapter(this, arrayOfPosts);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        // Populate feed
        List<User> userList = IslandDB.getUsers();
        for(User user: userList)
        {
            List<Post> userPosts = IslandDB.getPostsForUser(user);
            arrayOfPosts.addAll(userPosts);
        }

        // Swipe to refresh
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);

        refreshLayout.setOnRefreshListener(() ->
        {
            // TODO: Run async task again
            refreshLayout.setRefreshing(false);
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

        if (id == R.id.nav_profile)
        {
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, Utils.getUser(this));
            startActivity(profileIntent);
        }
        else if (id == R.id.nav_friends)
        {
            startActivity(new Intent(this, ViewFriendsActivity.class));
        }
        else if (id == R.id.nav_settings)
        {

        }
        else if (id == R.id.qr_code)
        {
            qrCodeActionDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: Do something with qr results
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
        {
            if(result.getContents() == null)
            {
                Log.d(TAG, "Cancelled scan");
            }
            else
            {
                Log.d(TAG, "Contents: " + result.getContents());
                // TODO: If contents are valid, open a dialog to allow use
            }
        }
        else
        {
            // This is important, otherwise the result will not be passed to the fragment
            //super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void startNewPostActivity(View view)
    {
        Intent newPostIntent = new Intent(FeedActivity.this, NewPostActivity.class);
        startActivity(newPostIntent);
    }

    public  void qrCodeActionDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.qr_action_dialog)
                .setItems(R.array.qr_actions, (DialogInterface dialog, int which) ->
                {
                    switch (which)
                    {
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
}
