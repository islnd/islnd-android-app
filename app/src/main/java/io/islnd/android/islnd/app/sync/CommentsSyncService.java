package io.islnd.android.islnd.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class CommentsSyncService extends Service {

    private static final String TAG = CommentsSyncService.class.getSimpleName();

    private static final Object sSyncAdapterLock = new Object();
    private static CommentsSyncAdapter sCommentsSyncAdapter = null;

    public void onCreate() {
        Log.d(TAG, "onCreate");
        synchronized (sSyncAdapterLock) {
            if (sCommentsSyncAdapter == null) {
                sCommentsSyncAdapter = new CommentsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sCommentsSyncAdapter.getSyncAdapterBinder();
    }
}
