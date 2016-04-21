package io.islnd.android.islnd.app.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.islnd.android.islnd.app.R;

public class NotificationViewHolder extends RecyclerView.ViewHolder  {
    public ImageView profileImageCircle;
    public ImageView profileImageSquare;
    public TextView notificationContent;
    public ImageView notificationTypeIcon;
    public TextView timestamp;
    public View view;

    public NotificationViewHolder(View itemView) {
        super(itemView);
        profileImageCircle = (ImageView) itemView.findViewById(R.id.profile_image_circle);
        profileImageSquare = (ImageView) itemView.findViewById(R.id.profile_image_square);
        notificationContent = (TextView) itemView.findViewById(R.id.notification_content);
        notificationTypeIcon = (ImageView) itemView.findViewById(R.id.notification_type_icon);
        timestamp = (TextView) itemView.findViewById(R.id.timestamp);
        view = itemView;
    }
}
