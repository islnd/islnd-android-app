package io.islnd.android.islnd.messaging.message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.crypto.EncryptedMessage;

public class MessagePublisher {
    private static final String TAG = MessagePublisher.class.getSimpleName();

    public static void removeFriend(Context context, int userId) {
        String newAlias = CryptoUtil.createAlias();
        ContentValues[] values = buildEncryptedMessagesToRemoveFriend(context, userId, newAlias);
        int recordsInserted = context.getContentResolver().bulkInsert(
                IslndContract.OutgoingMessageEntry.CONTENT_URI,
                values
        );
        Log.v(TAG, String.format("wrote %d messages", recordsInserted));

        //--Update my alias if bulk insert successful
        DataUtils.updateAlias(context, IslndContract.UserEntry.MY_USER_ID, newAlias);
    }

    private static ContentValues[] buildEncryptedMessagesToRemoveFriend(Context context, int userId, String newAlias) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_PUBLIC_KEY,
                IslndContract.UserEntry.COLUMN_MESSAGE_INBOX
        };

        Cursor cursor = null;
        try {
            final String selection = IslndContract.UserEntry._ID + " != ? AND " +
                    IslndContract.UserEntry._ID + " != ?";
            cursor = context.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    selection,
                    new String[] {
                            Integer.toString(IslndContract.UserEntry.MY_USER_ID),
                            Integer.toString(userId)
                    },
                    null);

            ContentValues[] values = new ContentValues[cursor.getCount()];
            int index = 0;
            if (cursor.moveToFirst()) {
                do {
                    values[index] = new ContentValues();
                    EncryptedMessage encryptedMessage = new EncryptedMessage(
                            MessageBuilder.buildNewAliasMessage(
                                    context,
                                    cursor.getString(1),
                                    newAlias
                            ),
                            CryptoUtil.decodePublicKey(cursor.getString(0))
                    );
                    values[index].put(
                            IslndContract.OutgoingMessageEntry.COLUMN_MAILBOX,
                            cursor.getString(1));
                    values[index].put(
                            IslndContract.OutgoingMessageEntry.COLUMN_BLOB,
                            encryptedMessage.getBlob());
                    index++;
                } while (cursor.moveToNext());
            }

            return values;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
