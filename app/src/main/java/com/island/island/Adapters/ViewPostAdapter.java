package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.island.island.Activities.ProfileActivity;
import com.island.island.Database.ProfileDatabase;
import com.island.island.Dialogs;
import com.island.island.Models.Comment;
import com.island.island.Models.Post;
import com.island.island.R;
import com.island.island.Utils.ImageUtils;
import com.island.island.Utils.Utils;
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
        Post post = (Post) mList.get(0);

        holder.postUserName.setText(post.getUserName());
        holder.postTimestamp.setText(Utils.smartTimestampFromUnixTime(post.getTimestamp()));
        holder.postContent.setText(post.getContent());

        // Go to profile on picture click
        holder.postProfileImage.setOnClickListener((View v) ->
        {
            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
            profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, post.getUserName());
            mContext.startActivity(profileIntent);
        });

        ProfileDatabase profileDatabase = ProfileDatabase.getInstance(mContext);
        Uri profileImageUri = Uri.parse(profileDatabase.getProfileImageUri(post.getUserName()));
        ImageUtils.setPostProfileImageSampled(mContext, holder.postProfileImage, profileImageUri);

        if(Utils.isUser(mContext, post.getUserName()))
        {
            holder.postOverflow.setVisibility(View.VISIBLE);

            holder.postOverflow.setOnClickListener((View v) ->
            {
                PopupMenu popup = new PopupMenu(mContext, holder.postOverflow);
                popup.getMenuInflater().inflate(R.menu.post_menu, popup.getMenu());
                popup.setOnMenuItemClickListener((MenuItem item) ->
                {
                    switch (item.getItemId())
                    {
                        case R.id.delete_post:
                            Dialogs.deletePostDialog(mContext, "");
                            // TODO: Behavior after removal?
                            // TODO: Don't have postIds yet
                    }

                    return true;
                });

                popup.show();
            });
        }
        else
        {
            holder.postOverflow.setVisibility(View.GONE);
        }
    }

    public void bindComment(CommentViewHolder holder, int position)
    {
        final Comment comment = (Comment) mList.get(position);
        final ImageView overflow = holder.overflow;

        holder.userName.setText(comment.getUsername());
        holder.comment.setText(comment.getComment());

        // Go to profile on picture click
        holder.profileImage.setOnClickListener((View v) ->
        {
            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
            profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, comment.getUsername());
            mContext.startActivity(profileIntent);
        });

        ProfileDatabase profileDatabase = ProfileDatabase.getInstance(mContext);
        Uri profileImageUri = Uri.parse(profileDatabase.getProfileImageUri(comment.getUsername()));
        ImageUtils.setCommentProfileImageSampled(mContext, holder.profileImage, profileImageUri);

        if(Utils.isUser(mContext, comment.getUsername()))
        {
            holder.overflow.setVisibility(View.VISIBLE);

            holder.overflow.setOnClickListener((View v) ->
            {
                PopupMenu popup = new PopupMenu(mContext, overflow);
                popup.getMenuInflater().inflate(R.menu.comment_menu, popup.getMenu());
                popup.setOnMenuItemClickListener((MenuItem item) ->
                {
                    switch (item.getItemId())
                    {
                        case R.id.delete_post:
                            Dialogs.deleteCommentDialog(mContext);
                            // TODO: Behavior after removal?
                            // TODO: Don't have postIds yet
                    }

                    return true;
                });

                popup.show();
            });
        }
        else
        {
            holder.overflow.setVisibility(View.GONE);
        }
    }
}
