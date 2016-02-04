package com.island.island.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.island.island.Adapters.ProfileAdapter;
import com.island.island.Containers.Post;
import com.island.island.Containers.Profile;
import com.island.island.Containers.User;
import com.island.island.Database.IslandDB;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity
{
    public static String USER_NAME_EXTRA = "USER_NAME";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Post list stuff
        ArrayList profileList = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.profile_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ProfileAdapter(profileList, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        // Get intent with username
        Intent profileIntent = getIntent();
        String userName = profileIntent.getStringExtra(USER_NAME_EXTRA);
        Profile profile = IslandDB.getUserProfile(userName);

        // Add profile first then posts
        profileList.add(profile);

        // User posts
        List<Post> userPosts = IslandDB.getPostsForUser(new User(userName, "", ""));
        profileList.addAll(userPosts);
    }

    public void startNewPostActivity(View view)
    {
        Intent newPostIntent = new Intent(ProfileActivity.this, NewPostActivity.class);
        startActivity(newPostIntent);
    }
}
