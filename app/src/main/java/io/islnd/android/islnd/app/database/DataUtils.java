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
import io.islnd.android.islnd.messaging.SecretIdentity;
import io.islnd.android.islnd.messaging.ServerTime;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.message.Message;

public class DataUtils {
    private static final String TAG = DataUtils.class.getSimpleName();

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
        final String publicKeyDigest = CryptoUtil.getDigest(publicKey);
        Log.v(TAG, "public key digest " + publicKeyDigest);
        userValues.put(IslndContract.UserEntry.COLUMN_PUBLIC_KEY_DIGEST,
                publicKeyDigest);
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

    public static void updateUserOutbox(Context context, String outbox, Key publicKey) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.UserEntry.COLUMN_MESSAGE_OUTBOX, outbox);

        String selection = IslndContract.UserEntry.COLUMN_PUBLIC_KEY + " = ?";
        String[] selectionArgs = new String[]{
                CryptoUtil.encodeKey(publicKey)
        };

        context.getContentResolver().update(
                IslndContract.UserEntry.CONTENT_URI,
                values,
                selection,
                selectionArgs
        );
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
        contentResolver.delete(IslndContract.NotificationEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.CommentEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.ProfileEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.AliasEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.DisplayNameEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.ReceivedEventEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.ReceivedMessageEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.OutgoingEventEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.OutgoingMessageEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.MessageTokenEntry.CONTENT_URI, null, null);

        contentResolver.delete(IslndContract.PostEntry.CONTENT_URI, null, null);

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

    public static boolean isUserActive(Context mContext, long userId) {
        String[] projection = {
                IslndContract.UserEntry.COLUMN_ACTIVE,
        };

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    IslndContract.UserEntry._ID + " = ?",
                    new String[] {Long.toString(userId)},
                    null
            );

            if (cursor.moveToFirst()) {
                return cursor.getInt(0) == IslndContract.UserEntry.ACTIVE;
            }

            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean userExists(Context mContext, PublicKey publicKey) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    new String[]{},
                    IslndContract.UserEntry.COLUMN_PUBLIC_KEY + " = ?",
                    new String[] {CryptoUtil.encodeKey(publicKey)},
                    null
            );

            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean userExists(Context mContext, long userId) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    new String[]{},
                    IslndContract.UserEntry._ID + " = ?",
                    new String[] {Long.toString(userId)},
                    null
            );

            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void deactivateUser(Context mContext, long userId) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.UserEntry.COLUMN_ACTIVE, IslndContract.UserEntry.NOT_ACTIVE);
        mContext.getContentResolver().update(
                IslndContract.UserEntry.CONTENT_URI,
                values,
                IslndContract.UserEntry._ID + " = ?",
                new String[] {Long.toString(userId)}
        );
    }

    public static boolean addOrUpdateUser(Context context, PublicKey publicKey, String inbox) {
        return addOrUpdateUser(context, publicKey, inbox, null);
    }

    public static boolean addOrUpdateUser(Context context, PublicKey publicKey, String inbox, String outbox) {
        boolean newUser = !userExists(context, publicKey);
        if (newUser) {
            addUser(context, publicKey, inbox, outbox);
        } else {
            updateUser(context, publicKey, inbox, outbox);
        }

        return newUser;
    }

    private static void addUser(Context context, PublicKey publicKey, String inbox, String outbox) {
        Log.d(TAG, "addUser");
        ContentValues values = new ContentValues();
        values.put(IslndContract.UserEntry.COLUMN_PUBLIC_KEY, CryptoUtil.encodeKey(publicKey));
        values.put(IslndContract.UserEntry.COLUMN_PUBLIC_KEY_DIGEST, CryptoUtil.getDigest(publicKey));
        values.put(IslndContract.UserEntry.COLUMN_MESSAGE_INBOX, inbox);
        if (outbox != null) {
            values.put(IslndContract.UserEntry.COLUMN_MESSAGE_OUTBOX, outbox);
        }

        context.getContentResolver().insert(
                IslndContract.UserEntry.CONTENT_URI,
                values);
    }

    private static void updateUser(Context context, PublicKey publicKey, String inbox, String outbox) {
        Log.d(TAG, "updateUser");
        ContentValues values = new ContentValues();
        values.put(IslndContract.UserEntry.COLUMN_MESSAGE_INBOX, inbox);
        values.put(IslndContract.UserEntry.COLUMN_ACTIVE, IslndContract.UserEntry.ACTIVE);
        if (outbox != null) {
            values.put(IslndContract.UserEntry.COLUMN_MESSAGE_OUTBOX, outbox);
        }

        String selection = IslndContract.UserEntry.COLUMN_PUBLIC_KEY + " = ?";
        String[] selectionArgs = new String[] { CryptoUtil.encodeKey(publicKey) };
        context.getContentResolver().update(
                IslndContract.UserEntry.CONTENT_URI,
                values,
                selection,
                selectionArgs);
    }

    public static void addOrUpdateSecretIdentity(Context context, int userId, SecretIdentity secretIdentity) {
        if (secretIdentityExists(context, userId)) {
            updateSecretIdentity(context, userId, secretIdentity);
        } else {
            addSecretIdentity(context, userId, secretIdentity);
        }
    }

    private static boolean secretIdentityExists(Context context, int userId) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.AliasEntry.CONTENT_URI,
                    new String[]{},
                    IslndContract.AliasEntry.COLUMN_USER_ID + " = ?",
                    new String[] {Long.toString(userId)},
                    null
            );

            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void updateSecretIdentity(Context context, int userId, SecretIdentity secretIdentity) {
        Log.d(TAG, "updateSecretIdentity");
        ContentValues values = new ContentValues();
        values.put(IslndContract.AliasEntry.COLUMN_USER_ID, userId);
        values.put(IslndContract.AliasEntry.COLUMN_ALIAS, secretIdentity.getAlias());
        values.put(IslndContract.AliasEntry.COLUMN_GROUP_KEY, CryptoUtil.encodeKey(secretIdentity.getGroupKey()));
        context.getContentResolver().update(
                IslndContract.AliasEntry.buildAliasWithUserId(userId),
                values,
                null,
                null);

        values.clear();
        values.put(IslndContract.DisplayNameEntry.COLUMN_USER_ID, userId);
        values.put(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME, secretIdentity.getDisplayName());
        context.getContentResolver().update(
                IslndContract.DisplayNameEntry.buildDisplayNameWithUserId(userId),
                values,
                null,
                null);
    }

    public static void addSecretIdentity(Context context, int userId, SecretIdentity secretIdentity) {
        Log.d(TAG, "addSecretIdentity");
        ContentValues values = new ContentValues();
        values.put(IslndContract.AliasEntry.COLUMN_USER_ID, userId);
        values.put(IslndContract.AliasEntry.COLUMN_ALIAS, secretIdentity.getAlias());
        values.put(IslndContract.AliasEntry.COLUMN_GROUP_KEY, CryptoUtil.encodeKey(secretIdentity.getGroupKey()));
        context.getContentResolver().insert(
                IslndContract.AliasEntry.CONTENT_URI,
                values);

        values.clear();
        values.put(IslndContract.DisplayNameEntry.COLUMN_USER_ID, userId);
        values.put(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME, secretIdentity.getDisplayName());
        context.getContentResolver().insert(
                IslndContract.DisplayNameEntry.CONTENT_URI,
                values);
    }

    public static boolean validateMessage(Context context, Message message) {
        if (message.getNonce() == null) {
            return false;
        }

        String[] projection = new String[] {IslndContract.MessageTokenEntry._ID};
        String selection = IslndContract.MessageTokenEntry.COLUMN_MAILBOX + " = ? AND " +
                IslndContract.MessageTokenEntry.COLUMN_NONCE + " = ?";
        String[] selectionArgs = new String[] {
                message.getMailbox(),
                message.getNonce()
        };

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.MessageTokenEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );

            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void addMessageToken(Context context, String mailbox, String nonce) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.MessageTokenEntry.COLUMN_MAILBOX, mailbox);
        values.put(IslndContract.MessageTokenEntry.COLUMN_NONCE, nonce);
        context.getContentResolver().insert(
                IslndContract.MessageTokenEntry.CONTENT_URI,
                values
        );
    }

    public static List<String> getActiveUserOutboxes(Context context) {
        List<String> outboxes = new ArrayList<>();
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_MESSAGE_OUTBOX
        };
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    IslndContract.UserEntry.COLUMN_ACTIVE + " = ?",
                    new String[] {Integer.toString(IslndContract.UserEntry.ACTIVE)},
                    null);
            if (!cursor.moveToFirst()) {
                return outboxes;
            }

            do {
                final String mailbox = cursor.getString(0);
                if (mailbox != null) {
                    outboxes.add(mailbox);
                }
            } while (cursor.moveToNext());

            return outboxes;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static List<String> getMessageTokenMailboxes(Context context) {
        List<String> mailboxes = new ArrayList<>();
        String[] projection = new String[] {
                IslndContract.MessageTokenEntry.COLUMN_MAILBOX
        };
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.MessageTokenEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
            if (!cursor.moveToFirst()) {
                return mailboxes;
            }

            do {
                final String mailbox = cursor.getString(0);
                if (mailbox != null) {
                    mailboxes.add(mailbox);
                }
            } while (cursor.moveToNext());

            return mailboxes;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
