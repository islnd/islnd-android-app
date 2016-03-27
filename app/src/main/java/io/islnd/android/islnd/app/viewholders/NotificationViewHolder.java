package io.islnd.android.islnd.app.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.islnd.android.islnd.app.R;

public class NotificationViewHolder extends RecyclerView.ViewHolder  {
    public ImageView profileImage;
    public TextView notificationContent;
    public ImageView notificationTypeIcon;
    public TextView timestamp;

    public NotificationViewHolder(View itemView) {
        super(itemView);
        profileImage = (ImageView) itemView.findViewById(R.id.profile_image);
        notificationContent = (TextView) itemView.findViewById(R.id.notification_content);
        notificationTypeIcon = (ImageView) itemView.findViewById(R.id.notification_type_icon);
        timestamp = (TextView) itemView.findViewById(R.id.timestamp);
    }
}
