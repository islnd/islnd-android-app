package io.islnd.android.islnd.messaging.event;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;

public class EventProcessor {
    private static ContentResolver mContentResolver;

    public static void process(Context context, Event event) {
        int eventType = event.getType();
        mContentResolver = context.getContentResolver();
        switch (eventType) {
            case EventType.CHANGE_DISPLAY_NAME: {
                changeDisplayName(context, (ChangeDisplayNameEvent) event);
                break;
            }
        }
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
