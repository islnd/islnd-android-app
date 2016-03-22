package io.islnd.android.islnd.app.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import io.islnd.android.islnd.app.IslndIntent;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;
import io.islnd.android.islnd.messaging.crypto.InvalidSignatureException;
import io.islnd.android.islnd.messaging.event.ChangeDisplayNameEvent;
import io.islnd.android.islnd.messaging.event.Event;
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

        boolean anyNewEventProcessed;
        do {
            anyNewEventProcessed = false;
            List<EncryptedEvent> encryptedEvents = getEncryptedEvents();
            if (encryptedEvents == null) {
                mContext.sendBroadcast(new Intent(IslndIntent.EVENT_SYNC_COMPLETE));
                Log.d(TAG, "event query returned null!");
                return;
            }

            Log.v(TAG, String.format("event service returned %d events", encryptedEvents.size()));
            PriorityQueue<Event> events = decryptEvents(encryptedEvents);

            //--Process events in order
            while (!events.isEmpty()) {
                boolean newEventProcessed = EventProcessor.process(mContext, events.poll());
                if (newEventProcessed) {
                    anyNewEventProcessed = true;
                }
            }
        } while (anyNewEventProcessed);

        mContext.sendBroadcast(new Intent(IslndIntent.EVENT_SYNC_COMPLETE));
        Log.v(TAG, "completed on perform sync");
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();

        mContext.sendBroadcast(new Intent(IslndIntent.EVENT_SYNC_COMPLETE));
        Log.d(TAG, "sync cancelled");
    }

    @NonNull
    private PriorityQueue<Event> decryptEvents(List<EncryptedEvent> encryptedEvents) {
        PriorityQueue<Event> events = new PriorityQueue<>();
        for (EncryptedEvent encryptedEvent : encryptedEvents) {
            String alias = encryptedEvent.getAlias();

            //--TODO need to handle multiple users having same alias
            int userId = DataUtils.getUserIdFromAlias(mContext, alias);

            //--TODO keys need to be a keystore or in-memory cache service
            Key groupKey = DataUtils.getGroupKey(mContext, userId);
            Key publicKey = DataUtils.getPublicKey(mContext, userId);
            try {
                final Event event = encryptedEvent.decryptAndVerify(groupKey, publicKey);
                if (event != null) {
                    events.add(event);
                }
            } catch (InvalidSignatureException e) {
                e.printStackTrace();
            }
        }
        return events;
    }

    private List<EncryptedEvent> getEncryptedEvents() {
        String[] projection = {
                IslndContract.AliasEntry.COLUMN_ALIAS
        };

        String[] args = new String[] { Integer.toString(Util.getUserId(mContext)) };
        Cursor cursor = mContentResolver.query(
                IslndContract.AliasEntry.CONTENT_URI,
                projection,
                IslndContract.AliasEntry.COLUMN_USER_ID + " != ?",
                args,
                null);

        EventQuery eventQuery = buildEventQuery(cursor);
        return Rest.postEventQuery(
                eventQuery,
                Util.getApiKey(getContext()));
    }

    @NonNull
    private EventQuery buildEventQuery(Cursor cursor) {
        List<String> aliases = new ArrayList<String>();
        if (!cursor.moveToFirst()) {
            return new EventQuery(aliases);
        }

        do {
            final String alias = cursor.getString(cursor.getColumnIndex(IslndContract.AliasEntry.COLUMN_ALIAS));
            Log.v(TAG, "query includes: " + alias);
            aliases.add(alias);
        } while (cursor.moveToNext());

        return new EventQuery(aliases);
    }
}
