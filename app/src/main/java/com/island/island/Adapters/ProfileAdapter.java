package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.island.island.Activities.EditProfileActivity;
import com.island.island.Activities.ProfileActivity;
import com.island.island.Activities.ViewPostActivity;
import com.island.island.Dialogs;
import com.island.island.Models.Post;
import com.island.island.Models.Profile;
import com.island.island.R;
import com.island.island.Utils.Utils;
import com.island.island.ViewHolders.GlancePostViewHolder;
import com.island.island.ViewHolders.ProfileViewHolder;

import java.util.ArrayList;

/**
 * Created by poo on 2/3/2016.
 */
public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private ArrayList mList = new ArrayList<>();
    private Context mContext = null;

    public ProfileAdapter(ArrayList list, Context context)
    {
        mList = list;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.glance_post, parent, false);
        RecyclerView.ViewHolder viewHolder = new GlancePostViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        bindPost((GlancePostViewHolder)holder, position);
    }

    @Override
    public int getItemCount()
    {
        return mList.size();
    }

    private void bindPost(GlancePostViewHolder holder, int position)
    {
        Post post = (Post) mList.get(position);

        holder.postUserName.setText(post.getUserName());
        holder.postTimestamp.setText(post.getTimestamp());
        holder.postContent.setText(post.getContent());
        holder.postCommentCount.setText(Utils.numberOfCommentsString(post.getComments().size()));

        // Go to profile on picture click
        holder.postProfileImage.setOnClickListener((View v) ->
        {
            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
            profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, post.getUserName());
            mContext.startActivity(profileIntent);
        });

        // View post on post click
        holder.itemView.setOnClickListener((View v) ->
        {
            Intent viewPostIntent = new Intent(mContext, ViewPostActivity.class);
            viewPostIntent.putExtra(Post.POST_EXTRA, post);
            mContext.startActivity(viewPostIntent);
        });

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

    private void bindProfile(ProfileViewHolder holder)
    {
        Profile profile = (Profile) mList.get(0);

        holder.userName.setText(profile.getUserName());
        holder.aboutMe.setText(profile.getAboutMe());

        // Client user's profile
        if(Utils.isUser(mContext, profile.getUserName()))
        {
            holder.editProfile.setVisibility(View.VISIBLE);
            holder.editProfile.setOnClickListener((View v) ->
            {
                mContext.startActivity(new Intent(mContext, EditProfileActivity.class));
            });
        }
    }
}
