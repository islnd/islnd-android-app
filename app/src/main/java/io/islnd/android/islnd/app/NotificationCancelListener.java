package io.islnd.android.islnd.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationCancelListener extends BroadcastReceiver {

    private static final String TAG = NotificationCancelListener.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "notification cancel onReceive");
        NotificationHelper.cancelNotification(context);
    }
}
