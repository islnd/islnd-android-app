package com.island.island.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.island.island.R;


public class ViewPostActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Boilerplate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get intent with post info
        Intent intent = getIntent();
        String postProfileName = intent.getStringExtra("POST_PROFILE_NAME");
        String postTimestamp = intent.getStringExtra("POST_TIMESTAMP");
        String postContent = intent.getStringExtra("POST_CONTENT");

        // Get layout views and set data
        TextView name = (TextView) findViewById(R.id.post_profile_name);
        TextView timestamp = (TextView) findViewById(R.id.post_timestamp);
        TextView content = (TextView) findViewById(R.id.post_content);

        name.setText(postProfileName);
        timestamp.setText(postTimestamp);
        content.setText(postContent);

        // Set FAB onclick
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
