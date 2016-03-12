package io.islnd.android.islnd.app.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.islnd.android.islnd.app.R;

public class CommentViewHolder extends RecyclerView.ViewHolder
{
    public ImageView profileImage;
    public TextView userName;
    public TextView comment;
    public TextView timestamp;
    public View view;

    public CommentViewHolder(View itemView)
    {
        super(itemView);
        profileImage = (ImageView) itemView.findViewById(R.id.comment_profile_image);
        userName = (TextView) itemView.findViewById(R.id.comment_user_name);
        comment = (TextView) itemView.findViewById(R.id.comment);
        timestamp = (TextView) itemView.findViewById(R.id.comment_timestamp);
        view = itemView;
    }
}
