package io.islnd.android.islnd.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class EventSyncService extends Service {

    private static final String TAG = EventSyncService.class.getSimpleName();

    private static final Object sSyncAdapterLock = new Object();
    private static EventSyncAdapter sEventSyncAdapter = null;

    public void onCreate() {
        Log.d(TAG, "onCreate");
        synchronized (sSyncAdapterLock) {
            if (sEventSyncAdapter == null) {
                sEventSyncAdapter = new EventSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sEventSyncAdapter.getSyncAdapterBinder();
    }
}
