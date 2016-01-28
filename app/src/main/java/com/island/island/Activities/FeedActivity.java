package com.island.island.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.island.island.Adapters.FeedAdapter;
import com.island.island.Containers.Post;
import com.island.island.Containers.Profile;
import com.island.island.Containers.User;
import com.island.island.Database.IslandDB;
import com.island.island.R;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Boilerplate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Nav drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Feed posts setup
        ArrayList<Post> arrayOfPosts = new ArrayList<>();
        FeedAdapter feedAdapter = new FeedAdapter(this, arrayOfPosts);
        ListView postsListView = (ListView) findViewById(R.id.feed_posts);
        postsListView.setAdapter(feedAdapter);

        // Populate feed
        List<User> userList = IslandDB.getUsers();
        for(int i = 0; i < userList.size(); ++i)
        {
            User user = userList.get(i);

            List<Post> userPosts = IslandDB.getPostsForUser(user);
            for(int j = 0; j < userPosts.size(); ++j)
            {
                feedAdapter.add(userPosts.get(j));
            }
        }

        // View post on post click
        postsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent viewPostIntent = new Intent(FeedActivity.this, ViewPostActivity.class);
                Post post = (Post) parent.getItemAtPosition(position);
                viewPostIntent.putExtra(Post.POST_EXTRA, post);
                startActivity(viewPostIntent);
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action
        } else if (id == R.id.nav_friends) {

        } else if (id == R.id.nav_settings) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // On clicks
    public void startNewPostActivity(View view)
    {
        Intent newPostIntent = new Intent(FeedActivity.this, NewPostActivity.class);
        startActivity(newPostIntent);
    }
}
