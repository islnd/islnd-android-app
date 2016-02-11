package com.island.island.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.island.island.R;

/**
 * Created by poo on 2/3/2016.
 */
public class ProfileViewHolder extends RecyclerView.ViewHolder
{
    public ImageView profileImage;
    public ImageView headerImage;
    public TextView userName;
    public TextView aboutMe;

    public ProfileViewHolder(View itemView)
    {
        super(itemView);
        profileImage = (ImageView) itemView.findViewById(R.id.profile_profile_image);
        headerImage = (ImageView) itemView.findViewById(R.id.profile_header_image);
        userName = (TextView) itemView.findViewById(R.id.profile_user_name);
        aboutMe = (TextView) itemView.findViewById(R.id.profile_about_me);
    }
}
