package com.island.island.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.island.island.R;

/**
 * Created by poo on 2/3/2016.
 */
public class ProfileViewHolder extends RecyclerView.ViewHolder
{
    public TextView userName;
    public TextView aboutMe;

    public ProfileViewHolder(View itemView)
    {
        super(itemView);
        userName = (TextView) itemView.findViewById(R.id.profile_name);
        aboutMe = (TextView) itemView.findViewById(R.id.profile_about_me);
    }
}
