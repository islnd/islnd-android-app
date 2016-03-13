package io.islnd.android.islnd.app.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.islnd.android.islnd.app.R;

/**
 * Created by poo on 2/3/2016.
 */
public class FriendGlanceViewHolder extends RecyclerView.ViewHolder
{
    public ImageView profileImage;
    public RelativeLayout overflow;
    public TextView userName;

    public FriendGlanceViewHolder(View itemView)
    {
        super(itemView);
        profileImage = (ImageView) itemView.findViewById(R.id.friend_profile_image);
        userName = (TextView) itemView.findViewById(R.id.friend_user_name);
        overflow = (RelativeLayout) itemView.findViewById(R.id.view_friend_overflow_layout);
    }
}
