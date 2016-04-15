package io.islnd.android.islnd.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.util.Util;

public class SyncAlarm extends BroadcastReceiver {

    private static final String TAG = SyncAlarm.class.getSimpleName();

    //public static final int SYNC_INTERVAL_MILLISECONDS = 1800000; // 30 minutes
    public static final int SYNC_INTERVAL_MILLISECONDS = 10000; // 30 minutes

    private StopSystemNotificationsReceiver mNotificationsReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");

        if (mNotificationsReceiver == null) {
            mNotificationsReceiver = new StopSystemNotificationsReceiver();
        }

        IntentFilter intentFilter = new IntentFilter(IslndAction.EVENT_SYNC_COMPLETE);
        context.getApplicationContext().registerReceiver(mNotificationsReceiver, intentFilter);

        NotificationHelper.startListening(context);

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

    public static void cancelAlarm(Context context) {
        Log.v(TAG, "cancel sync alarm");
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, SyncAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.cancel(pendingIntent);
    }

    public static void enableReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, SyncAlarm.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void disableReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, SyncAlarm.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
