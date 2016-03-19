package io.islnd.android.islnd.messaging.event;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.PostKey;

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
            case EventType.NEW_POST: {
                addPost(context, (NewPostEvent) event);
                break;
            }
            case EventType.DELETE_POST: {
                deletePost(context, (DeletePostEvent) event);
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

    private static void addPost(Context context, NewPostEvent newPostEvent) {
        int postUserId = DataUtils.getUserIdFromAlias(context, newPostEvent.getAlias());
        ContentValues values = new ContentValues();
        values.put(IslndContract.PostEntry.COLUMN_USER_ID, postUserId);
        values.put(IslndContract.PostEntry.COLUMN_POST_ID, newPostEvent.getPostId());
        values.put(IslndContract.PostEntry.COLUMN_CONTENT, newPostEvent.getContent());
        values.put(IslndContract.PostEntry.COLUMN_TIMESTAMP, newPostEvent.getTimestamp());
        context.getContentResolver().insert(
                IslndContract.PostEntry.CONTENT_URI,
                values);
    }

    private static void deletePost(Context context, DeletePostEvent deletePostEvent) {
        int postUserId = DataUtils.getUserIdFromAlias(context, deletePostEvent.getAlias());
        PostKey postToDelete = new PostKey(postUserId, deletePostEvent.getPostId());
        DataUtils.deletePost(context, postToDelete);
    }
}
