package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.island.island.Activities.EditProfileActivity;
import com.island.island.Activities.ProfileActivity;
import com.island.island.Activities.ViewPostActivity;
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

    private static final int PROFILE_HEADER = 0;
    private static final int POST = 1;

    public ProfileAdapter(ArrayList list, Context context)
    {
        mList = list;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == POST)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.glance_post, parent, false);
            viewHolder = new GlancePostViewHolder(v);
        }
        else if(viewType == PROFILE_HEADER)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.profile_header, parent, false);
            viewHolder = new ProfileViewHolder(v);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        if(position != 0)
        {
            bindPost((GlancePostViewHolder)holder, position);
        }
        else
        {
            bindProfile((ProfileViewHolder) holder);
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
        return position == 0 ? PROFILE_HEADER : POST;
    }

    private void bindPost(GlancePostViewHolder holder, int position)
    {
        final Post post = (Post) mList.get(position);

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

    private void bindProfile(ProfileViewHolder holder)
    {
        Profile profile = (Profile) mList.get(0);

        holder.userName.setText(profile.getUserName());
        holder.aboutMe.setText(profile.getAboutMe());

        // Client user's profile
        if(Utils.isUser(mContext, profile.getUserName()))
        {
            holder.editProfile.setVisibility(View.VISIBLE);
            holder.editProfile.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mContext.startActivity(new Intent(mContext, EditProfileActivity.class));
                }
            });
        }
    }
}
