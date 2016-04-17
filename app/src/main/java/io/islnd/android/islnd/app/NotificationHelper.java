package io.islnd.android.islnd.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.islnd.android.islnd.app.activities.NavBaseActivity;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.database.NotificationType;

public class NotificationHelper {

    private static final String TAG = NotificationHelper.class.getSimpleName();

    private static final int NOTIFICATION_ID = 7403;

    private static NotificationObserver mNotificationObserver;

    public static void startListening(Context context) {
        Log.v(TAG, "start listening");
        if (mNotificationObserver == null) {
            mNotificationObserver = new NotificationObserver(new Handler() , context);
        }

        context.getContentResolver().registerContentObserver(
                IslndContract.NotificationEntry.CONTENT_URI,
                true,
                mNotificationObserver
        );
    }

    public static void stopListening(Context context) {
        Log.v(TAG, "stop listening");
        if (mNotificationObserver == null) {
            return;
        }

        context.getContentResolver().unregisterContentObserver(mNotificationObserver);
    }

    private static void updateNotification(Context context) {
        Log.v(TAG, "updateNotification");

        // Build active notifications
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        List<SpannableStringBuilder> activeNotifications = getSpannableActiveNotificationsStrings(context);

        String bigContentTitle = "";
        int notificationCount = activeNotifications.size();
        if (notificationCount == 0) {
            Log.d(TAG, "no active notifications, exiting updateNotification");
            return;
        } else if (notificationCount == 1) {
            bigContentTitle = context.getString(R.string.notification_big_content_title_single);
        } else {
            bigContentTitle = Integer.toString(notificationCount)
                    + " "
                    + context.getString(R.string.notification_big_content_title);
        }
        inboxStyle.setBigContentTitle(bigContentTitle);

        for (int i = 0; i < notificationCount; ++i) {
            inboxStyle.addLine(activeNotifications.get(i));
        }

        // Handle notification cancel
        PendingIntent deleteIntent = PendingIntent.getBroadcast(
                context,
                0,
                new Intent(context, NotificationCancelListener.class),
                0);

        // Open notifications fragment
        Intent resultIntent = new Intent(context, NavBaseActivity.class);
        resultIntent.setAction(IslndAction.NOTIFICATION_CONTENT_CLICK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(NavBaseActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        // Build and notify
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(bigContentTitle)
                .setContentText(activeNotifications.get(0))
                .setDeleteIntent(deleteIntent)
                .setStyle(inboxStyle)
                .setContentIntent(resultPendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setCategory(Notification.CATEGORY_SOCIAL)
                .setColor(ContextCompat.getColor(context, R.color.notification_primary))
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void setNotificationsToNotActive(Context context) {
        Log.v(TAG, "setNotificationsToNotActive");

        ContentValues values = new ContentValues();
        values.put(
                IslndContract.NotificationEntry.COLUMN_ACTIVE,
                IslndContract.NotificationEntry.NOT_ACTIVE);

        String selection = IslndContract.NotificationEntry.COLUMN_ACTIVE + " = ?";
        String[] selectionArgs = new String[]{
                Integer.toString(IslndContract.NotificationEntry.ACTIVE)
        };

        context.getContentResolver().update(
                IslndContract.NotificationEntry.CONTENT_URI,
                values,
                selection,
                selectionArgs
        );
    }

    public static void cancelNotifications(Context context) {
        setNotificationsToNotActive(context);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private static List<SpannableStringBuilder> getSpannableActiveNotificationsStrings(Context context) {
        List<SpannableStringBuilder> activeNotifications = new ArrayList<>();

        String[] projection = new String[] {
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_NOTIFICATION_USER_ID,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_NOTIFICATION_TYPE
        };

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.NotificationEntry.CONTENT_URI,
                    projection,
                    IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_ACTIVE + " = ?",
                    new String[] {Integer.toString(IslndContract.NotificationEntry.ACTIVE)},
                    IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_TIMESTAMP + " DESC");
            while (cursor.moveToNext()) {
                int userId = cursor.getInt(cursor.getColumnIndex(IslndContract.NotificationEntry.COLUMN_NOTIFICATION_USER_ID));
                int notificationType = cursor.getInt(cursor.getColumnIndex(IslndContract.NotificationEntry.COLUMN_NOTIFICATION_TYPE));
                String displayName = DataUtils.getDisplayName(context, userId);
                activeNotifications.add(buildSpannableNotificationString(
                        context,
                        displayName,
                        notificationType)
                );
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return activeNotifications;
    }

    public static SpannableStringBuilder buildSpannableNotificationString(Context context,
                                                                  String displayName,
                                                                  int notificationType) {
        String contentInfo;

        switch (notificationType) {
            case NotificationType.COMMENT:
                contentInfo = displayName + " " + context.getString(R.string.notification_comment_content);
                break;
            case NotificationType.NEW_FRIEND:
                contentInfo = displayName + " " + context.getString(R.string.notification_new_friend_content);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(contentInfo);
        StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        stringBuilder.setSpan(styleSpan, 0, displayName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return stringBuilder;
    }

    private static class NotificationObserver extends ContentObserver {

        private static final String TAG = NotificationObserver.class.getSimpleName();

        private Context mContext;

        public NotificationObserver(Handler handler, Context context) {
            super(handler);
            mContext = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.v(TAG, "onChange");
            updateNotification(mContext);
        }
    }
}
