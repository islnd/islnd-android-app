package io.islnd.android.islnd.app.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.security.Key;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKey;

import io.islnd.android.islnd.app.models.CommentKey;
import io.islnd.android.islnd.app.models.PostAliasKey;
import io.islnd.android.islnd.app.models.PostKey;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.messaging.Identity;
import io.islnd.android.islnd.messaging.ServerTime;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

public class DataUtils {
    private static final String TAG = DataUtils.class.getSimpleName();

    public static long insertUser(Context context, Identity identity, String messageOutbox) {
        return insertUser(
                context,
                identity.getDisplayName(),
                identity.getAlias(),
                identity.getMessageInbox(),
                messageOutbox,
                identity.getGroupKey(),
                identity.getPublicKey());
    }

    public static long insertUser(
            Context context,
            String displayName,
            String alias,
            String messageInbox,
            String messageOutbox,
            Key groupKey,
            PublicKey publicKey) {
        ContentValues userValues = new ContentValues();
        final String encodedPublicKey = CryptoUtil.encodeKey(publicKey);
        userValues.put(IslndContract.UserEntry.COLUMN_PUBLIC_KEY, encodedPublicKey);
        userValues.put(IslndContract.UserEntry.COLUMN_MESSAGE_INBOX, messageInbox);
        userValues.put(IslndContract.UserEntry.COLUMN_MESSAGE_OUTBOX, messageOutbox);

        final ContentResolver contentResolver = context.getContentResolver();
        Uri uri = contentResolver.insert(
                IslndContract.UserEntry.CONTENT_URI,
                userValues);

        if (uri == null) {
            Log.d(TAG, "failed to insert user!");
        }

        long userId = DataUtils.getUserIdFromPublicKey(context, publicKey);
        Log.v(TAG, "user inserted with id " + userId);

        ContentValues displayNameValues = new ContentValues();
        displayNameValues.put(IslndContract.DisplayNameEntry.COLUMN_USER_ID, userId);
        displayNameValues.put(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME, displayName);
        contentResolver.insert(
                IslndContract.DisplayNameEntry.CONTENT_URI,
                displayNameValues);

        ContentValues aliasValues = new ContentValues();
        aliasValues.put(IslndContract.AliasEntry.COLUMN_USER_ID, userId);
        aliasValues.put(IslndContract.AliasEntry.COLUMN_ALIAS, alias);
        aliasValues.put(IslndContract.AliasEntry.COLUMN_GROUP_KEY, CryptoUtil.encodeKey(groupKey));
        contentResolver.insert(
                IslndContract.AliasEntry.CONTENT_URI,
                aliasValues);

        Log.v(TAG, "inserted user");
        Log.v(TAG, "alias " + alias);
        Log.v(TAG, "profile alias " + messageInbox);

        return userId;
    }

    public static String getMostRecentAlias(Context context, int userId) {
        String[] projection = new String[] {
                IslndContract.AliasEntry.COLUMN_ALIAS,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.AliasEntry.buildAliasWithUserId(userId),
                projection,
                null,
                null,
                IslndContract.AliasEntry._ID + " DESC");

        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }

            return null;
        } finally {
            cursor.close();
        }
    }

    public static SecretKey getGroupKey(Context context, int userId) {
        Log.v(TAG, "get group key for user " + userId);
        String[] projection = new String[] {
                IslndContract.AliasEntry.COLUMN_GROUP_KEY,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.AliasEntry.buildAliasWithUserId(userId),
                projection,
                null,
                null,
                null);

        try {
            if (cursor.moveToFirst()) {
                return CryptoUtil.decodeSymmetricKey(cursor.getString(0));
            }

            return null;
        } finally {
            cursor.close();
        }
    }

    public static String getDisplayName(Context context, int userId) {
        String[] projection = new String[] {
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.DisplayNameEntry.buildDisplayNameWithUserId(userId),
                projection,
                null,
                null,
                null);

        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }

            return null;
        } finally {
            cursor.close();
        }
    }

    public static PublicKey getPublicKey(Context context, int userId) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_PUBLIC_KEY
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.UserEntry.buildUserWithUserId(userId),
                projection,
                null,
                null,
                null);

        try {
            if (cursor.moveToFirst()) {
                return CryptoUtil.decodePublicKey(cursor.getString(0));
            }

            return null;
        } finally {
            cursor.close();
        }
    }

    public static int getUserIdForMessageOutbox(Context context, String mailbox) {
        String[] projection = new String[] {
                IslndContract.UserEntry._ID,
        };

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    IslndContract.UserEntry.COLUMN_MESSAGE_OUTBOX + " = ?",
                    new String[] {mailbox},
                    null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                return -1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int getUserIdFromPublicKey(Context context, PublicKey publicKey) {
        String[] projection = new String[] {
                IslndContract.UserEntry._ID,
        };

        String encodedPublicKey = CryptoUtil.encodeKey(publicKey);
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    IslndContract.UserEntry.COLUMN_PUBLIC_KEY + " = ?",
                    new String[] {encodedPublicKey},
                    null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                throw new IllegalArgumentException("database has no entry for public key: " + encodedPublicKey);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int getUserIdFromAlias(Context context, String alias) {
        String[] projection = new String[] {
                IslndContract.UserEntry._ID,
        };

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.AliasEntry.CONTENT_URI,
                    projection,
                    IslndContract.AliasEntry.COLUMN_ALIAS + " = ?",
                    new String[] {alias},
                    null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                throw new IllegalArgumentException("database has no entry for alias: " + alias);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int getUserIdFromPostAuthorAlias(Context context, String alias) {
        String[] projection = new String[] {
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_USER_ID,
        };

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.PostEntry.CONTENT_URI,
                    projection,
                    IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_ALIAS + " = ?",
                    new String[] {alias},
                    null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                throw new IllegalArgumentException("database has no entry for alias: " + alias);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void deletePost(Context context, PostKey postKey) {
        Log.v(TAG, "delete post " + postKey);
        String selection = IslndContract.PostEntry.COLUMN_USER_ID + " = ? AND " +
                IslndContract.PostEntry.COLUMN_POST_ID + " = ?";
        String[] args = new String[] {
                Integer.toString(postKey.getUserId()),
                postKey.getPostId()};
        context.getContentResolver().delete(
                IslndContract.PostEntry.CONTENT_URI,
                selection,
                args);
    }

    public static void deleteComment(ContentResolver contentResolver, CommentKey commentKey) {
        String selection = IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID + " = ? AND " +
                IslndContract.CommentEntry.COLUMN_COMMENT_ID + " = ?";
        String[] args = new String[] {
                Integer.toString(commentKey.getCommentAuthorId()),
                commentKey.getCommentId()};
        contentResolver.delete(
                IslndContract.CommentEntry.CONTENT_URI,
                selection,
                args);
    }

    public static void deleteComment(Context context, CommentKey commentKey) {
        deleteComment(context.getContentResolver(), commentKey);
    }

    public static void deleteAll(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(IslndContract.ProfileEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.CommentEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.PostEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.AliasEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.DisplayNameEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.ReceivedEventEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.ReceivedMessageEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.OutgoingEventEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.OutgoingMessageEntry.CONTENT_URI, null, null);

        contentResolver.delete(IslndContract.UserEntry.CONTENT_URI, null, null);
    }

    public static void insertProfile(Context context, Profile profile, long userId) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.ProfileEntry.COLUMN_USER_ID, userId);
        values.put(IslndContract.ProfileEntry.COLUMN_ABOUT_ME, profile.getAboutMe());
        values.put(
                IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI,
                profile.getHeaderImageUri().toString());
        values.put(
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI,
                profile.getProfileImageUri().toString());

        context.getContentResolver().insert(IslndContract.ProfileEntry.CONTENT_URI, values);
    }

    public static boolean activeUserHasPublicKey(Context context, PublicKey publicKey) {
        String[] projection = new String[0];

        Cursor cursor = null;
        try {
            final String selection = IslndContract.UserEntry.COLUMN_PUBLIC_KEY + " = ? AND " +
                    IslndContract.UserEntry.COLUMN_ACTIVE + " = " + IslndContract.UserEntry.ACTIVE;
            final String[] selectionArgs = {CryptoUtil.encodeKey(publicKey)};
            cursor = context.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);
            boolean result = cursor.moveToFirst();
            Log.v(TAG, "active user has public key " + result);
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean inactiveUserHasPublicKey(Context context, PublicKey publicKey) {
        String[] projection = new String[0];

        Cursor cursor = null;
        try {
            final String selection = IslndContract.UserEntry.COLUMN_PUBLIC_KEY + " = ? AND " +
                    IslndContract.UserEntry.COLUMN_ACTIVE + " = " + IslndContract.UserEntry.NOT_ACTIVE;
            final String[] selectionArgs = {CryptoUtil.encodeKey(publicKey)};
            cursor = context.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);
            boolean result = cursor.moveToFirst();
            Log.v(TAG, "inactive user has public key " + result);
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static long activateAndUpdateUser(Context context, Identity identity, String messageOutbox) {
        Cursor cursor = null;
        try {
            final String selection = IslndContract.UserEntry.COLUMN_PUBLIC_KEY + " = ?";
            final String[] selectionArgs = {CryptoUtil.encodeKey(identity.getPublicKey())};
            ContentValues values = new ContentValues();
            values.put(IslndContract.UserEntry.COLUMN_MESSAGE_OUTBOX, messageOutbox);
            values.put(IslndContract.UserEntry.COLUMN_MESSAGE_INBOX, identity.getMessageInbox());
            values.put(IslndContract.UserEntry.COLUMN_ACTIVE, IslndContract.UserEntry.ACTIVE);
            int rowsUpdated = context.getContentResolver().update(
                    IslndContract.UserEntry.CONTENT_URI,
                    values,
                    selection,
                    selectionArgs);
            Log.v(TAG, "updated user rows " + rowsUpdated);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        int userId = getUserIdFromPublicKey(context, identity.getPublicKey());
        Log.v(TAG, "updating user id " + userId);
        Log.v(TAG, "new alias " + identity.getAlias());
        Log.v(TAG, "new group key " + CryptoUtil.encodeKey(identity.getGroupKey()));
        try {
            ContentValues values = new ContentValues();
            values.put(IslndContract.AliasEntry.COLUMN_ALIAS, identity.getAlias());
            values.put(IslndContract.AliasEntry.COLUMN_GROUP_KEY, CryptoUtil.encodeKey(identity.getGroupKey()));
            context.getContentResolver().update(
                    IslndContract.AliasEntry.buildAliasWithUserId(userId),
                    values,
                    null,
                    null);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return userId;
    }

    public static int getCommentCount(Context context, String postAuthorAlias, String postId) {
        String[] projection = new String[] { IslndContract.PostEntry.COLUMN_COMMENT_COUNT };

        String selection = IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_ALIAS + " = ? AND " +
                IslndContract.PostEntry.COLUMN_POST_ID + " = ?";
        String[] selectionArgs = new String[] {
                postAuthorAlias,
                postId
        };

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.PostEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);
            Log.v(TAG, "cursor has " + cursor.getCount() + " rows.");
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                throw new IllegalArgumentException(
                        String.format("database has no entry for post user id %s post id %d",
                                postAuthorAlias,
                                postId));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static PostAliasKey getParentPostFromComment(Context context, int commmentAuthorUserId, String commentId) {
        String[] projection = new String[] {
                IslndContract.CommentEntry.COLUMN_POST_AUTHOR_ALIAS,
                IslndContract.CommentEntry.COLUMN_POST_ID
        };

        String selection =
                IslndContract.CommentEntry.TABLE_NAME + "." + IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID + " = ? AND " +
                IslndContract.CommentEntry.COLUMN_COMMENT_ID + " = ?";
        String[] selectionArgs = new String[] {
                Integer.toString(commmentAuthorUserId),
                commentId
        };

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.CommentEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);
            if (cursor.moveToFirst()) {
                return new PostAliasKey(
                        cursor.getString(0),
                        cursor.getString(1));
            } else {
                throw new IllegalArgumentException(
                        String.format("database has no entry for comment user id %s comment id %s",
                                commmentAuthorUserId,
                                commentId));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void insertNotification(Context context,
                                          int userId,
                                          int notificationType,
                                          String postId,
                                          long timestamp) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.NotificationEntry.COLUMN_NOTIFICATION_USER_ID, userId);
        values.put(IslndContract.NotificationEntry.COLUMN_NOTIFICATION_TYPE, notificationType);
        values.put(IslndContract.NotificationEntry.COLUMN_POST_ID, postId);
        values.put(IslndContract.NotificationEntry.COLUMN_TIMESTAMP, timestamp);
        context.getContentResolver().insert(
                IslndContract.NotificationEntry.CONTENT_URI,
                values);
        Log.v(TAG, "notification added");
    }

    public static void insertNewFriendNotification(Context context, int userId) {
        insertNotification(
                context,
                userId,
                NotificationType.NEW_FRIEND,
                null,
                ServerTime.getCurrentTimeMillis(context));
    }

    public static void insertNewCommentNotification(Context context,
                                                    int userId,
                                                    String postId,
                                                    long timestamp) {
        insertNotification(context,
                userId,
                NotificationType.COMMENT,
                postId,
                timestamp);
    }

    public static String getMessageInbox(Context context, int userId) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_MESSAGE_INBOX,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.UserEntry.buildUserWithUserId(userId),
                projection,
                null,
                null,
                null);

        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }

            return null;
        } finally {
            cursor.close();
        }
    }

    public static Profile getProfile(Context context, int userId) {
        String[] projection = new String[] {
                IslndContract.ProfileEntry.COLUMN_ABOUT_ME,
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI,
                IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.ProfileEntry.buildProfileUriWithUserId(userId),
                projection,
                null,
                null,
                null);

        try {
            if (cursor.moveToFirst()) {
                return new Profile(
                        cursor.getString(0),
                        Uri.parse(cursor.getString(1)),
                        Uri.parse(cursor.getString(2)));
            }

            return null;
        } finally {
            cursor.close();
        }
    }

    public static void updateMyUserMailbox(Context context, String newMailbox) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.UserEntry.COLUMN_MESSAGE_INBOX, newMailbox);

        String selection = IslndContract.UserEntry._ID + " = ?";
        String[] selectionArgs = new String[]{
                Integer.toString(IslndContract.UserEntry.MY_USER_ID)
        };

        context.getContentResolver().update(
                IslndContract.UserEntry.CONTENT_URI,
                values,
                selection,
                selectionArgs
        );
    }

    public static Key getPublicKeyForUserInbox(Context context, String inbox) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_PUBLIC_KEY,
        };

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    IslndContract.UserEntry.COLUMN_MESSAGE_INBOX + " = ?",
                    new String[] {inbox},
                    null);
            if (cursor.moveToFirst()) {
                return CryptoUtil.decodePublicKey(cursor.getString(0));
            } else {
                throw new IllegalArgumentException("database has no entry for inbox: " + inbox);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static PublicKey getPublicKeyForUserOutbox(Context context, String outbox) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_PUBLIC_KEY,
        };

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    IslndContract.UserEntry.COLUMN_MESSAGE_OUTBOX + " = ?",
                    new String[] {outbox},
                    null);
            if (cursor.moveToFirst()) {
                return CryptoUtil.decodePublicKey(cursor.getString(0));
            } else {
                throw new IllegalArgumentException("database has no entry for outbox: " + outbox);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void updateAlias(Context context, int userId, String newAlias) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.AliasEntry.COLUMN_ALIAS, newAlias);

        context.getContentResolver().update(
                IslndContract.AliasEntry.buildAliasWithUserId(userId),
                values,
                null,
                null);
    }

    public static void markUserAsDeletedAndDeletePosts(Context context, int userId) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(
                IslndContract.PostEntry.buildPostUriWithUserId(userId),
                null,
                null
        );

        ContentValues values = new ContentValues();
        values.put(IslndContract.UserEntry.COLUMN_ACTIVE, IslndContract.UserEntry.NOT_ACTIVE);
        contentResolver.update(
                IslndContract.UserEntry.buildUserWithUserId(userId),
                values,
                null,
                null
        );
    }
}
