package com.island.island.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.island.island.Containers.Post;
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
        final Post post = (Post)intent.getSerializableExtra(Post.POST_EXTRA);

        // Get layout views and set data
        ImageView postProfilePicture = (ImageView) findViewById(R.id.post_profile_image);
        TextView name = (TextView) findViewById(R.id.post_user_name);
        TextView timestamp = (TextView) findViewById(R.id.post_timestamp);
        TextView content = (TextView) findViewById(R.id.post_content);

        name.setText(post.getUserName());
        timestamp.setText(post.getTimestamp());
        content.setText(post.getContent());

        // Go to profile on picture click
        postProfilePicture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent profileIntent = new Intent(ViewPostActivity.this, ProfileActivity.class);
                profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, post.getUserName());
                startActivity(profileIntent);
            }
        });
    }
}
