package io.islnd.android.islnd.app;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.crypto.EncryptedMessage;
import io.islnd.android.islnd.messaging.message.Message;
import io.islnd.android.islnd.messaging.message.MessageProcessor;
import io.islnd.android.islnd.messaging.server.MessageQuery;

public class FindNewFriendService extends Service {

    private static final String TAG = FindNewFriendService.class.getSimpleName();

    private Context mContext;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");
        mContext = this;

        checkMailbox();

        return START_NOT_STICKY;
    }

    private void checkMailbox() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                int[] delay = {10000, 5000, 5000, 10000, 20000, 20000, 20000};

                for (int i = 0; i < delay.length; i++) {
                    mContext.getContentResolver().requestSync(
                            Util.getSyncAccount(mContext),
                            IslndContract.CONTENT_AUTHORITY,
                            new Bundle());

                    try {
                        Thread.sleep(delay[i]);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "sleep interrupted");
                        Log.d(TAG, e.toString());
                    }
                }

                stopSelf();
                return null;
            }
        }.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
