package io.islnd.android.islnd.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.islnd.android.islnd.app.NotificationHelper;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.activities.ProfileActivity;
import io.islnd.android.islnd.app.activities.ViewPostActivity;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.database.NotificationType;
import io.islnd.android.islnd.app.models.Post;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.app.viewholders.NotificationViewHolder;

public class NotificationAdapter extends CursorRecyclerViewAdapter<NotificationViewHolder> {

    private static final String TAG = NotificationAdapter.class.getSimpleName();

    public NotificationAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, Cursor cursor) {
        String displayName = cursor.getString(cursor.getColumnIndex(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME));
        int userId = cursor.getInt(cursor.getColumnIndex(IslndContract.NotificationEntry.COLUMN_NOTIFICATION_USER_ID));
        int notificationType = cursor.getInt(cursor.getColumnIndex(IslndContract.NotificationEntry.COLUMN_NOTIFICATION_TYPE));

        switch (notificationType) {
            case NotificationType.COMMENT: {
                holder.notificationTypeIcon.setImageResource(R.drawable.ic_comment_18dp);

                String postId =
                        cursor.getString(cursor.getColumnIndex(IslndContract.NotificationEntry.COLUMN_POST_ID));

                holder.view.setOnClickListener((View v) -> {
                    Intent intent = new Intent(mContext, ViewPostActivity.class);
                    intent.putExtra(
                            Post.POST_ID_EXTRA,
                            postId);
                    intent.putExtra(
                            Post.POST_USER_ID_EXTRA,
                            IslndContract.UserEntry.MY_USER_ID);
                    mContext.startActivity(intent);
                });
                break;
            }
            case NotificationType.NEW_FRIEND: {
                holder.notificationTypeIcon.setImageResource(R.drawable.ic_person_add_18dp);

                holder.view.setOnClickListener((View v) -> {
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.USER_ID_EXTRA, userId);
                    mContext.startActivity(intent);
                });
                break;
            }
        }

        holder.notificationContent.setText(NotificationHelper.buildSpannableNotificationString(
                mContext,
                displayName,
                notificationType)
        );

        holder.timestamp.setText(Util.smartTimestampFromUnixTime(
                mContext,
                cursor.getLong(cursor.getColumnIndex(IslndContract.NotificationEntry.COLUMN_TIMESTAMP)))
        );

        ImageUtil.setPostProfileImageSampled(
                mContext,
                holder.profileImage,
                Uri.parse(cursor.getString(cursor.getColumnIndex(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI)))
        );
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification, parent, false);

        NotificationViewHolder viewHolder = new NotificationViewHolder(v);

        return viewHolder;
    }
}
