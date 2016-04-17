package io.islnd.android.islnd.app.activities;

import android.support.v7.app.AppCompatActivity;

import io.islnd.android.islnd.app.SyncAlarm;

public class IslndActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        SyncAlarm.cancelAlarm(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        SyncAlarm.setAlarm(getApplicationContext(), SyncAlarm.SYNC_INTERVAL_MILLISECONDS);
    }
}
