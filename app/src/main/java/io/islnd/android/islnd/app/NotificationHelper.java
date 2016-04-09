package io.islnd.android.islnd.app;

import android.util.Log;

public class NotificationHelper {

    private static final String TAG = NotificationHelper.class.getSimpleName();

    public static final String CANCEL_NOTIFICATION = "CANCEL_NOTIFICATION";

    private static final int NOTIFICATION_ID = 7403;

    public static void updateNotification() {
        Log.v(TAG, "updateNotification");
    }

    public static void cancelNotification() {
        Log.v(TAG, "cancelNotification");
    }
}
