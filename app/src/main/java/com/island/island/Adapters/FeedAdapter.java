package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.island.island.Activities.ProfileActivity;
import com.island.island.Activities.ViewPostActivity;
import com.island.island.Containers.Post;
import com.island.island.R;
import com.island.island.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 2/2/16.
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder>
{
    List<Post> mPosts = new ArrayList<>();
    Context mContext;

    public FeedAdapter(List<Post> posts, Context context)
    {
        mPosts = posts;
        mContext = context;
    }

    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.glance_post, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        final Post post = mPosts.get(position);

        holder.postUserName.setText(post.getUserName());
        holder.postTimestamp.setText(post.getTimestamp());
        holder.postContent.setText(post.getContent());

        // Go to profile on picture click
        holder.postProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, post.getUserName());
                mContext.startActivity(profileIntent);
            }
        });

        final int mPosition = position;

        holder.mItemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent viewPostIntent = new Intent(mContext, ViewPostActivity.class);
                Post post = mPosts.get(mPosition);
                viewPostIntent.putExtra(Post.POST_EXTRA, post);
                mContext.startActivity(viewPostIntent);
            }
        });

        // Set number of comments
        holder.postCommentCount.setText(Utils.numberOfCommentsString(post.getComments().size()));
    }

    @Override
    public int getItemCount()
    {
        return mPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView postProfileImage;
        TextView postUserName;
        TextView postTimestamp;
        TextView postContent;
        TextView postCommentCount;
        View mItemView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mItemView = itemView;
            postProfileImage = (ImageView) itemView.findViewById(R.id.post_profile_image);
            postUserName = (TextView) itemView.findViewById(R.id.post_user_name);
            postTimestamp = (TextView) itemView.findViewById(R.id.post_timestamp);
            postContent = (TextView) itemView.findViewById(R.id.post_content);
            postCommentCount = (TextView) itemView.findViewById(R.id.post_comment_count);
        }
    }
}
