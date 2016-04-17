package io.islnd.android.islnd.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StopSystemNotificationsReceiver extends BroadcastReceiver {

    private static final String TAG = StopSystemNotificationsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");
        NotificationHelper.stopListening(context);
        context.getApplicationContext().unregisterReceiver(this);
    }
}
