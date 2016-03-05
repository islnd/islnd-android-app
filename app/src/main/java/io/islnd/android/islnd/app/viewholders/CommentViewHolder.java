package io.islnd.android.islnd.app.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.islnd.android.islnd.app.R;

public class CommentViewHolder extends RecyclerView.ViewHolder
{
    public ImageView profileImage;
    public TextView userName;
    public TextView comment;
    public ImageView overflow;

    public CommentViewHolder(View itemView)
    {
        super(itemView);
        profileImage = (ImageView) itemView.findViewById(R.id.comment_profile_image);
        userName = (TextView) itemView.findViewById(R.id.comment_user_name);
        comment = (TextView) itemView.findViewById(R.id.comment);
        overflow = (ImageView) itemView.findViewById(R.id.comment_overflow);
    }
}
