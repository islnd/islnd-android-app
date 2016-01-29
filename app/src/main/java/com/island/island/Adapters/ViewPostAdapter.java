package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.island.island.Activities.ProfileActivity;
import com.island.island.Containers.Comment;
import com.island.island.Containers.Post;
import com.island.island.R;

import java.util.ArrayList;

/**
 * Created by poo on 1/29/2016.
 */
public class ViewPostAdapter extends ArrayAdapter
{
    private Context mContext;

    private static final int POST = 0;
    private static final int COMMENT = 1;

    ArrayList posts = new ArrayList<>();

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public int getItemViewType(int position)
    {
        return position == 0 ? POST : COMMENT;
    }

    public ViewPostAdapter(Context context, ArrayList list)
    {
        super(context, 0, list);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            // Post
            if(position == 0)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.interact_post,
                        parent, false);
                buildPost(convertView);
            }
            // Comment
            else
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment, parent,
                        false);
                buildComment(position, convertView);
            }
        }

        return convertView;
    }

    private void buildPost(View convertView)
    {
        final Post post = (Post) getItem(0);

        // Get layout views and set data
        ImageView postProfilePicture = (ImageView) convertView.findViewById(
                R.id.post_profile_image);
        TextView postName = (TextView) convertView.findViewById(R.id.post_user_name);
        TextView postTimestamp = (TextView) convertView.findViewById(R.id.post_timestamp);
        TextView postContent = (TextView) convertView.findViewById(R.id.post_content);

        postName.setText(post.getUserName());
        postTimestamp.setText(post.getTimestamp());
        postContent.setText(post.getContent());

        // Go to profile on picture click
        postProfilePicture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, post.getUserName());
                mContext.startActivity(profileIntent);
            }
        });
    }

    private void buildComment(int position, View convertView)
    {
        final Comment comment = (Comment) getItem(position);

        // Get layout views and set data
        ImageView commentProfileImage = (ImageView) convertView.findViewById(
                R.id.comment_profile_image);
        TextView commentUserName = (TextView) convertView.findViewById(R.id.comment_user_name);
        TextView commentView = (TextView) convertView.findViewById(R.id.comment);

        commentUserName.setText(comment.getUserName());
        commentView.setText(comment.getComment());

        // Go to profile on picture click
        commentProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, comment.getUserName());
                mContext.startActivity(profileIntent);
            }
        });

    }
}