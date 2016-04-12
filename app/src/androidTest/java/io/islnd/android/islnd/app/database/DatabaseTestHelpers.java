package io.islnd.android.islnd.app.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import java.security.PublicKey;

import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

public class DatabaseTestHelpers {
    public static long insertFakeUser(Context context) {
        ContentValues userValues = new ContentValues();
        userValues.put(IslndContract.UserEntry.COLUMN_PUBLIC_KEY, "key");
        userValues.put(IslndContract.UserEntry.COLUMN_MESSAGE_INBOX, "inbox");
        Uri result = context.getContentResolver().insert(
                IslndContract.UserEntry.CONTENT_URI,
                userValues
        );

        return IslndContract.UserEntry.getUserIdFromUri(result);
    }

    public static long insertFakeUser(Context context, PublicKey publicKey) {
        ContentValues userValues = new ContentValues();
        userValues.put(IslndContract.UserEntry.COLUMN_PUBLIC_KEY, CryptoUtil.encodeKey(publicKey));
        userValues.put(IslndContract.UserEntry.COLUMN_MESSAGE_INBOX, "inbox");
        Uri result = context.getContentResolver().insert(
                IslndContract.UserEntry.CONTENT_URI,
                userValues
        );

        final int userIdFromUri = IslndContract.UserEntry.getUserIdFromUri(result);
        ContentValues aliasValues = new ContentValues();
        aliasValues.put(IslndContract.AliasEntry.COLUMN_ALIAS, "fakeAlias");
        aliasValues.put(IslndContract.AliasEntry.COLUMN_GROUP_KEY, CryptoUtil.encodeKey(CryptoUtil.getKey()));
        aliasValues.put(IslndContract.AliasEntry.COLUMN_USER_ID, userIdFromUri);
        context.getContentResolver().insert(
                IslndContract.AliasEntry.CONTENT_URI,
                aliasValues
        );

        return userIdFromUri;
    }

    public static void setDisplayName(Context context, long userId, String newDisplayName) {
        ContentValues newValues = new ContentValues();
        newValues.put(IslndContract.DisplayNameEntry.COLUMN_USER_ID, userId);
        newValues.put(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME, newDisplayName);
        context.getContentResolver().insert(
                IslndContract.DisplayNameEntry.CONTENT_URI,
                newValues
        );
    }

    public static void clearTables(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(IslndContract.PostEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.CommentEntry.CONTENT_URI, null, null);

        contentResolver.delete(IslndContract.ProfileEntry.CONTENT_URI, null, null);

        contentResolver.delete(IslndContract.DisplayNameEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.AliasEntry.CONTENT_URI, null, null);

        contentResolver.delete(IslndContract.NotificationEntry.CONTENT_URI, null, null);

        contentResolver.delete(IslndContract.ReceivedEventEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.OutgoingEventEntry.CONTENT_URI, null, null);

        contentResolver.delete(IslndContract.ReceivedMessageEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.OutgoingMessageEntry.CONTENT_URI, null, null);

        contentResolver.delete(IslndContract.UserEntry.CONTENT_URI, null, null);
    }
}
