package com.island.island.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.island.island.Adapters.FeedAdapter;
import com.island.island.Adapters.ViewFriendsAdapter;
import com.island.island.Containers.Post;
import com.island.island.Containers.User;
import com.island.island.Database.IslandDB;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ViewFriendsActivity extends AppCompatActivity
{
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Feed posts setup
        List<User> arrayOfFriends = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.view_friends_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ViewFriendsAdapter(arrayOfFriends, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        List<User> friends = IslandDB.getUsers();
        for(int i = 0; i < friends.size(); ++i)
        {
            arrayOfFriends.add(friends.get(i));
        }
    }
}
