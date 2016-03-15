package io.islnd.android.islnd.messaging.event;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;

public class EventProcessor {
    private static final String TAG = EventProcessor.class.getSimpleName();
    private static ContentResolver mContentResolver;

    public static void process(Context context, Event event) {
        Log.v(TAG, "processing " + event);
        mContentResolver = context.getContentResolver();
        if (alreadyProcessed(event)) {
            return;
        }

        int eventType = event.getType();
        switch (eventType) {
            case EventType.CHANGE_DISPLAY_NAME: {
                changeDisplayName(context, (ChangeDisplayNameEvent) event);
                break;
            }
        }

        recordEventProcessed(event);
    }

    private static void recordEventProcessed(Event event) {
        mContentResolver.insert(
                IslndContract.EventEntry.buildEventUriWithPseudonymAndEventId(event),
                new ContentValues()
        );
    }

    private static boolean alreadyProcessed(Event event) {
        String[] projection = new String[] {
                IslndContract.EventEntry._ID
        };

        Cursor cursor = null;
        boolean alreadyProcessed;
        try {
            cursor = mContentResolver.query(
                    IslndContract.EventEntry.buildEventUriWithPseudonymAndEventId(event),
                    projection,
                    null,
                    null,
                    null
            );

            alreadyProcessed = cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (alreadyProcessed) {
            Log.v(TAG, "already processed " + event);
        }

        return alreadyProcessed;
    }

    private static void changeDisplayName(Context context, ChangeDisplayNameEvent event) {
        int userId = DataUtils.getUserIdFromAlias(context, event.getAlias());
        ContentValues values = new ContentValues();
        values.put(
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME,
                event.getNewDisplayName());

        mContentResolver.update(
                IslndContract.DisplayNameEntry.buildDisplayNameWithUserId(userId),
                values,
                null,
                null
        );
    }
}
