package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.island.island.Activities.FeedActivity;
import com.island.island.Activities.ProfileActivity;
import com.island.island.Containers.Post;
import com.island.island.R;

import java.util.ArrayList;

/**
 * Created by poo on 1/18/2016.
 */
public class FeedAdapter extends ArrayAdapter<Post>
{
    private Context mContext;
    public FeedAdapter(Context context, ArrayList posts)
    {
        super(context, 0, posts);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.post, parent, false);
        }

        final Post post = (Post) getItem(position);

        // Get layout views and set data
        ImageView postProfilePicture = (ImageView) convertView.findViewById(
                R.id.post_profile_picture);
        TextView postName = (TextView) convertView.findViewById(R.id.post_profile_name);
        TextView postTimestamp = (TextView) convertView.findViewById(R.id.post_timestamp);
        TextView postContent = (TextView) convertView.findViewById(R.id.post_content);

        postName.setText(post.profileName);
        postTimestamp.setText(post.timestamp);
        postContent.setText(post.content);

        // Go to profile on picture click
        postProfilePicture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, post.profileName);
                mContext.startActivity(profileIntent);
            }
        });

        return convertView;
    }
}
