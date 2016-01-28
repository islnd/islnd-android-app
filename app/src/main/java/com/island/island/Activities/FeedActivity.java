package com.island.island.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

            }
        });
    }
}
