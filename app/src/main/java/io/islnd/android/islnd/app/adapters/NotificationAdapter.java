package io.islnd.android.islnd.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.activities.ViewPostActivity;
import io.islnd.android.islnd.app.database.DataUtils;
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
        String contentInfo = "";
        String displayName = cursor.getString(cursor.getColumnIndex(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME));

        switch (cursor.getInt(cursor.getColumnIndex(IslndContract.NotificationEntry.COLUMN_NOTIFICATION_TYPE))) {
            case NotificationType.COMMENT:
                contentInfo = displayName + " " + mContext.getString(R.string.notification_comment_content);
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
                            Util.getUserId(mContext));
                    mContext.startActivity(intent);
                });
                break;
            case NotificationType.NEW_FRIEND:
                contentInfo = displayName + " " + mContext.getString(R.string.notification_new_friend_content);
                holder.notificationTypeIcon.setImageResource(R.drawable.ic_person_add_24dp);
                break;
        }

        final SpannableStringBuilder stringBuilder = new SpannableStringBuilder(contentInfo);
        final StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        stringBuilder.setSpan(styleSpan, 0, displayName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        holder.notificationContent.setText(stringBuilder);

        holder.timestamp.setText(Util.smartTimestampFromUnixTime(mContext, cursor.getLong(cursor.getColumnIndex(IslndContract.NotificationEntry.COLUMN_TIMESTAMP))));

        ImageUtil.setPostProfileImageSampled(
                mContext,
                holder.profileImage,
                Uri.parse(cursor.getString(cursor.getColumnIndex(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI))));
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification, parent, false);

        NotificationViewHolder viewHolder = new NotificationViewHolder(v);

        return viewHolder;
    }
}
