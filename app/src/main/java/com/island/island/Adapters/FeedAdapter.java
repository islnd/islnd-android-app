package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.island.island.Activities.ProfileActivity;
import com.island.island.Activities.ViewPostActivity;
import com.island.island.Models.Post;
import com.island.island.R;
import com.island.island.Utils.Utils;
import com.island.island.ViewHolders.GlancePostViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 2/2/16.
 */
public class FeedAdapter extends RecyclerView.Adapter<GlancePostViewHolder>
{
    private List<Post> mPosts = new ArrayList<>();
    private Context mContext = null;

    public FeedAdapter(List<Post> posts, Context context)
    {
        mPosts = posts;
        mContext = context;
    }

    @Override
    public GlancePostViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.glance_post, parent, false);

        GlancePostViewHolder viewHolder = new GlancePostViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(GlancePostViewHolder holder, int position)
    {
        final Post post = mPosts.get(position);

        holder.postUserName.setText(post.getUserName());
        holder.postTimestamp.setText(post.getTimestamp());
        holder.postContent.setText(post.getContent());
        holder.postCommentCount.setText(Utils.numberOfCommentsString(post.getComments().size()));

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

        // View post on post click
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent viewPostIntent = new Intent(mContext, ViewPostActivity.class);
                viewPostIntent.putExtra(Post.POST_EXTRA, post);
                mContext.startActivity(viewPostIntent);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mPosts.size();
    }
}
