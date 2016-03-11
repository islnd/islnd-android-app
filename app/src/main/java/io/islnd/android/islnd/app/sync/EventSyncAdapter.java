package io.islnd.android.islnd.app.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;
import io.islnd.android.islnd.messaging.crypto.InvalidSignatureException;
import io.islnd.android.islnd.messaging.event.ChangeDisplayNameEvent;
import io.islnd.android.islnd.messaging.event.EventProcessor;
import io.islnd.android.islnd.messaging.server.EventQuery;

public class EventSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = EventSyncAdapter.class.getSimpleName();

    private Context mContext;
    private ContentResolver mContentResolver;

    public EventSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        init(context);
    }

    public EventSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.v(TAG, "starting event sync");

        String[] projection = {
                IslndContract.AliasEntry.COLUMN_ALIAS,
                IslndContract.AliasEntry.COLUMN_GROUP_KEY,
        };
        Cursor cursor = mContentResolver.query(
                IslndContract.AliasEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        EventQuery eventQuery = buildEventQuery(cursor);

        //--Run query
        List<EncryptedEvent> encryptedEvents = Rest.postEventQuery(
                eventQuery,
                Util.getApiKey(getContext()));
        if (encryptedEvents == null) {
            Log.d(TAG, "event query returned null!");
            return;
        }

        Log.v(TAG, String.format("event service returned %d events", encryptedEvents.size()));

        //--Decrypt and process the events
        for (EncryptedEvent encryptedEvent : encryptedEvents) {
            String alias = encryptedEvent.getAlias();

            //--TODO need to handle multiple users having same alias
            int userId = DataUtils.getUserIdFromAlias(mContext, alias);

            //--TODO these need to be a keystore or in-memory cache service
            Key groupKey = DataUtils.getGroupKey(mContext, userId);
            Key publicKey = DataUtils.getPublicKey(mContext, userId);
            try {
                ChangeDisplayNameEvent event = encryptedEvent.decryptAndVerify(groupKey, publicKey);
                EventProcessor.process(mContext, event);
            } catch (InvalidSignatureException e) {
                e.printStackTrace();
            }
        }

        Log.v(TAG, "completed on perform sync");
    }

    @NonNull
    private EventQuery buildEventQuery(Cursor cursor) {
        List<String> aliases = new ArrayList<String>();
        if (!cursor.moveToFirst()) {
            return new EventQuery(aliases);
        }

        do {
            aliases.add(cursor.getString(cursor.getColumnIndex(IslndContract.AliasEntry.COLUMN_ALIAS)));
        } while (cursor.moveToNext());

        return new EventQuery(aliases);
    }
}