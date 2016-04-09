package io.islnd.android.islnd.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationHelper {

    private static final String TAG = NotificationHelper.class.getSimpleName();

    private static final int NOTIFICATION_ID = 7403;

    public static void updateNotification(Context context) {
        Log.v(TAG, "updateNotification");

        // Handle notification cancel
        PendingIntent deleteIntent = PendingIntent.getBroadcast(
                context,
                0,
                new Intent(context, NotificationCancelListener.class),
                0);

        // Build active notifications
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String[] activeNotifications = getActiveNotifications();
        String bigContentTitle = Integer.toString(activeNotifications.length)
                + " "
                + context.getString(R.string.notification_big_content_title);
        inboxStyle.setBigContentTitle(bigContentTitle);

        for (int i = 0; i < activeNotifications.length; ++i) {
            inboxStyle.addLine(activeNotifications[i]);
        }

        // Build and notify
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(bigContentTitle)
                .setContentText(activeNotifications[0])
                .setDeleteIntent(deleteIntent)
                .setStyle(inboxStyle);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void cancelNotification() {
        Log.v(TAG, "cancelNotification");
    }

    private static String[] getActiveNotifications() {
        String[] activeNotifications = {"Test 1", "Test 2"};
        return activeNotifications;
    }
}
