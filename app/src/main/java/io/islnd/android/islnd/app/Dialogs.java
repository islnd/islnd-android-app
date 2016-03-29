package io.islnd.android.islnd.app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;

import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.crypto.EncryptedMessage;
import io.islnd.android.islnd.messaging.message.MessageBuilder;

public class Dialogs {
    private static final String TAG = Dialogs.class.getSimpleName();

    public static void removeFriendDialog(Context context, int userId, String displayName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
        builder.setMessage("Remove " + displayName + " as a friend?")
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                {
                    String newAlias = CryptoUtil.createAlias();
                    ContentValues[] values = buildEncryptedMessagesToRemoveFriend(context, userId, newAlias);
                    context.getContentResolver().bulkInsert(
                            IslndContract.OutgoingMessageEntry.CONTENT_URI,
                            values
                    );

                    //--Update my alias if bulk insert successful
                    updateMyAlias(context, newAlias);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
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

    private static void updateMyAlias(Context context, String newAlias) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.AliasEntry.COLUMN_ALIAS, newAlias);

        context.getContentResolver().update(
                IslndContract.AliasEntry.buildAliasWithUserId(IslndContract.UserEntry.MY_USER_ID),
                values,
                null,
                null);
    }
}
