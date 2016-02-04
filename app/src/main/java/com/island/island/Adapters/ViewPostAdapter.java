package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.island.island.Activities.ProfileActivity;
import com.island.island.Containers.Comment;
import com.island.island.Containers.Post;
import com.island.island.R;
import com.island.island.ViewHolders.CommentViewHolder;
import com.island.island.ViewHolders.PostViewHolder;

import java.util.ArrayList;

/**
 * Created by poo on 2/3/2016.
 */
public class ViewPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private ArrayList mList = new ArrayList<>();
    private Context mContext = null;

    private static final int POST = 0;
    private static final int COMMENT = 1;

    public ViewPostAdapter(ArrayList list, Context context)
    {
        mList = list;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == COMMENT)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.comment, parent, false);
            viewHolder = new CommentViewHolder(v);
        }
        else if(viewType == POST)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.interact_post, parent, false);
            viewHolder = new PostViewHolder(v);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        if(position != 0)
        {
            bindComment((CommentViewHolder) holder, position);
        }
        else
        {
            bindPost((PostViewHolder) holder);
        }
    }

    @Override
    public int getItemCount()
    {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        return position == 0 ? POST : COMMENT;
    }

    public void bindPost(PostViewHolder holder)
    {
        final Post post = (Post) mList.get(0);

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
    }

    public void bindComment(CommentViewHolder holder, int position)
    {
        final Comment comment = (Comment) mList.get(position);

        holder.userName.setText(comment.getUserName());
        holder.comment.setText(comment.getComment());

        // Go to profile on picture click
        holder.profileImage.setOnClickListener(new View.OnClickListener()
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
