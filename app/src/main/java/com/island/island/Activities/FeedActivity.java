package com.island.island.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Feed posts
        ArrayList<Post> arrayOfPosts = new ArrayList<>();
        FeedAdapter feedAdapter = new FeedAdapter(this, arrayOfPosts);
        ListView postsListView = (ListView) findViewById(R.id.feed_posts);
        postsListView.setAdapter(feedAdapter);

        // Add test posts
        Post testPost = new Post("", "David Thompson", "Jan 2015", "Hello, World!");
        Post testPost1 = new Post("", "David Thompson", "Jan 2015", "Hello, World!");
        Post testPost2 = new Post("", "David Thompson", "Jan 2015", "Hello, World!");
        Post testPost3 = new Post("", "David Thompson", "Jan 2015", "Hello, World!");
        Post testPost4 = new Post("", "David Thompson", "Jan 2015", "Hello, World!");
        Post testPost5 = new Post("", "David Thompson", "Jan 2015", "Hello, World!");
        Post testPost6 = new Post("", "David Thompson", "Jan 2015", "Hello, World!");

        feedAdapter.add(testPost);
        feedAdapter.add(testPost1);
        feedAdapter.add(testPost2);
        feedAdapter.add(testPost3);
        feedAdapter.add(testPost4);
        feedAdapter.add(testPost5);
        feedAdapter.add(testPost6);

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

        // Profile test
        final Intent profileIntent = new Intent(this, ProfileActivity.class);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(profileIntent);
            }
        });
    }

}
