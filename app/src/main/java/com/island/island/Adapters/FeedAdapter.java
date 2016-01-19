package com.island.island.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.island.island.Containers.Post;
import com.island.island.R;

import java.util.ArrayList;

/**
 * Created by poo on 1/18/2016.
 */
public class FeedAdapter extends ArrayAdapter<Post>
{
    public FeedAdapter(Context context, ArrayList posts)
    {
        super(context, 0, posts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.post, parent, false);
        }

        Post post = (Post) getItem(position);

        // Get layout views
        TextView postName = (TextView) convertView.findViewById(R.id.post_profile_name);
        TextView postTimestamp = (TextView) convertView.findViewById(R.id.post_timestamp);
        TextView postContent = (TextView) convertView.findViewById(R.id.post_content);

        // Set values
        postName.setText(post.profileName);
        postTimestamp.setText(post.timestamp);
        postContent.setText(post.content);

        return convertView;
    }
}
