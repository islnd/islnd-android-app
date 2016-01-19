package com.island.island.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.island.island.Adapters.ProfileAdapter;
import com.island.island.Containers.Post;
import com.island.island.Containers.Profile;
import com.island.island.R;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Post list stuff
        ArrayList arrayOfPosts = new ArrayList<>();
        ProfileAdapter profileAdapter = new ProfileAdapter(this, arrayOfPosts);
        ListView postsListView = (ListView) findViewById(R.id.profile_posts);
        postsListView.setAdapter(profileAdapter);

        // Add test posts
        Profile profile = new Profile("", "", "David Thompson", "This is about me!");
        Post testPost = new Post("", "David Thompson", "Jan 2015", "Hello, World!");
        Post testPost1 = new Post("", "David Thompson", "Jan 2015", "Hello, World!");
        Post testPost2 = new Post("", "David Thompson", "Jan 2015", "Hello, World!");
        Post testPost3 = new Post("", "David Thompson", "Jan 2015", "Hello, World!");
        Post testPost4 = new Post("", "David Thompson", "Jan 2015", "Hello, World!");
        Post testPost5 = new Post("", "David Thompson", "Jan 2015", "Hello, World!");
        Post testPost6 = new Post("", "David Thompson", "Jan 2015", "Hello, World!");

        profileAdapter.add(profile);
        profileAdapter.add(testPost);
        profileAdapter.add(testPost1);
        profileAdapter.add(testPost2);
        profileAdapter.add(testPost3);
        profileAdapter.add(testPost4);
        profileAdapter.add(testPost5);
        profileAdapter.add(testPost6);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
