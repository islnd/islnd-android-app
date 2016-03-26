package io.islnd.android.islnd.app.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.islnd.android.islnd.app.R;

public class GlancePostViewHolder extends RecyclerView.ViewHolder {
    public ImageView postProfileImage;
    public TextView postUserName;
    public TextView postTimestamp;
    public TextView postContent;
    public TextView postCommentCount;
    public View view;

    public GlancePostViewHolder(View itemView) {
        super(itemView);
        postProfileImage = (ImageView) itemView.findViewById(R.id.post_profile_image);
        postUserName = (TextView) itemView.findViewById(R.id.post_user_name);
        postTimestamp = (TextView) itemView.findViewById(R.id.post_timestamp);
        postContent = (TextView) itemView.findViewById(R.id.post_content);
        postCommentCount = (TextView) itemView.findViewById(R.id.post_comment_count);
        view = itemView;
    }
}
