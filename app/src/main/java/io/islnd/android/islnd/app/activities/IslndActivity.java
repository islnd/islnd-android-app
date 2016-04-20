package io.islnd.android.islnd.app.activities;

import android.support.v7.app.AppCompatActivity;

import io.islnd.android.islnd.app.SyncAlarm;
import io.islnd.android.islnd.app.util.Util;

public class IslndActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        Util.applyAppTheme(getApplicationContext());
        SyncAlarm.cancelAlarm(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        SyncAlarm.setAlarm(getApplicationContext(), SyncAlarm.SYNC_INTERVAL_MILLISECONDS);
    }
}
