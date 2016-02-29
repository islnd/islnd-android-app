package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.island.island.Activities.ProfileActivity;
import com.island.island.Database.ProfileDatabase;
import com.island.island.DeleteCommentFragment;
import com.island.island.DeletePostFragment;
import com.island.island.Models.CommentViewModel;
import com.island.island.Models.Post;
import com.island.island.R;
import com.island.island.Utils.ImageUtils;
import com.island.island.Utils.Utils;
import com.island.island.ViewHolders.CommentViewHolder;
import com.island.island.ViewHolders.PostViewHolder;

import java.util.ArrayList;

public class ViewPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private static final String TAG = ViewPostAdapter.class.getSimpleName();

    private ArrayList mList = new ArrayList<>();
    private Context mContext = null;

    private static final int POST = 0;
    private static final int COMMENT = 1;
    private Post mPost;

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
        mPost = (Post) mList.get(0);

        holder.postUserName.setText(mPost.getUserName());
        holder.postTimestamp.setText(Utils.smartTimestampFromUnixTime(mPost.getTimestamp()));
        holder.postContent.setText(mPost.getContent());

        // Go to profile on picture click
        holder.postProfileImage.setOnClickListener(
                (View v) ->
                {
                    Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                    profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, mPost.getUserName());
                    mContext.startActivity(profileIntent);
                });

        ProfileDatabase profileDatabase = ProfileDatabase.getInstance(mContext);
        Uri profileImageUri = Uri.parse(profileDatabase.getProfileImageUri(mPost.getUserName()));
        ImageUtils.setPostProfileImageSampled(mContext, holder.postProfileImage, profileImageUri);

        if(Utils.isUser(mContext, mPost.getUserName()))
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
                            DialogFragment deletePostFragment =
                                    DeletePostFragment.buildWithArgs(mPost.getUserId(), mPost.getPostId());
                            deletePostFragment.show(
                                    ((FragmentActivity) mContext).getSupportFragmentManager(),
                                    mContext.getString(R.string.fragment_delete_post));
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
        final CommentViewModel comment = (CommentViewModel) mList.get(position);
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
                        case R.id.delete_comment:
                            DialogFragment deleteCommentFragment =
                                    DeleteCommentFragment.buildWithArgs(
                                            mPost.getUserId(),
                                            mPost.getPostId(),
                                            comment.getUserId(),
                                            comment.getCommentId());
                            deleteCommentFragment.show(
                                    ((FragmentActivity) mContext).getSupportFragmentManager(),
                                    mContext.getString(R.string.fragment_delete_comment));
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
