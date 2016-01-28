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

import com.island.island.Adapters.ProfileAdapter;
import com.island.island.Containers.Post;
import com.island.island.Containers.Profile;
import com.island.island.Containers.User;
import com.island.island.Database.IslandDB;
import com.island.island.R;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity
{
    public static String USER_NAME_EXTRA = "USER_NAME";

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

        // Get intent with username
        Intent profileIntent = getIntent();
        String userName = profileIntent.getStringExtra(USER_NAME_EXTRA);
        Profile profile = IslandDB.getUserProfile(userName);

        // Add profile first then posts
        profileAdapter.add(profile);

        // User posts
        List<Post> userPosts = IslandDB.getPostsForUser(new User(userName, "", ""));
        for(int i = 0; i < userPosts.size(); ++i)
        {
            profileAdapter.add(userPosts.get(i));
        }

        // View post on post click
        postsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Ignore profile header
                if(position == 0)
                {
                    return;
                }

                Intent viewPostIntent = new Intent(ProfileActivity.this, ViewPostActivity.class);
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
