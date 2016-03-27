package io.islnd.android.islnd.app;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Identity;
import io.islnd.android.islnd.messaging.MessageLayer;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.message.Message;
import io.islnd.android.islnd.messaging.server.MessageQuery;
import io.islnd.android.islnd.messaging.server.MessageQueryResponse;

public class FindNewFriendService extends Service {

    private static final String TAG = FindNewFriendService.class.getSimpleName();

    private Context mContext;
    private ContentResolver mContentResolver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");
        mContext = this;
        mContentResolver = getContentResolver();

        checkMailbox();

        return START_NOT_STICKY;
    }

    private void checkMailbox() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                int[] delay = {5000, 5000, 5000, 5000, 8000, 10000, 10000, 10000, 10000};


                for (int i = 0; i < delay.length; i++) {
                    List<String> mailboxes = getMailboxes();
                    MessageQuery messageQuery = new MessageQuery(mailboxes);

                    List<Message> messages = Rest.postMessageQuery(
                            messageQuery,
                            Util.getApiKey(mContext));

                    if (messages != null
                            && messages.size() > 0) {
                        Identity friendToAdd = Identity.fromProto(messages.get(0).getBlob());
                        MessageLayer.addFriendToDatabaseAndCreateDefaultProfile(mContext, friendToAdd);
                        break;
                    }

                    try {
                        Thread.sleep(delay[i]);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                stopSelf();
                return null;
            }
        }.execute();
    }

    @NonNull
    private List<String> getMailboxes() {
        String[] projection = new String[] { IslndContract.UserEntry.COLUMN_MESSAGE_INBOX};
        Cursor cursor = mContentResolver.query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
        List<String> mailboxes = new ArrayList<>();
        if (!cursor.moveToFirst()) {
            return mailboxes;
        }

        do {
            mailboxes.add(cursor.getString(0));
        } while (cursor.moveToNext());

        return mailboxes;
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
