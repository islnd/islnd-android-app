package io.islnd.android.islnd.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.util.Util;

public class SyncAlarm extends BroadcastReceiver {

    private static final String TAG = SyncAlarm.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");
        context.getContentResolver().requestSync(
                Util.getSyncAccount(context),
                IslndContract.CONTENT_AUTHORITY,
                new Bundle());
    }

    public static void setAlarm(Context context, int intervalInMillis) {
        SyncAlarm.cancelAlarm(context);
        Log.v(TAG, "set sync alarm with interval of : " + intervalInMillis + " milliseconds");
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, SyncAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                0,
                intervalInMillis,
                pendingIntent);
    }

    private static void cancelAlarm(Context context) {
        Log.v(TAG, "cancel sync alarm");
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, SyncAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.cancel(pendingIntent);
    }
}
