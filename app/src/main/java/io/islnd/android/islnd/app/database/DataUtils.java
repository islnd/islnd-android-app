package io.islnd.android.islnd.app.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.security.Key;

import io.islnd.android.islnd.app.models.CommentKey;
import io.islnd.android.islnd.app.models.PostAliasKey;
import io.islnd.android.islnd.app.models.PostKey;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.messaging.Identity;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

public class DataUtils {
    private static final String TAG = DataUtils.class.getSimpleName();

    public static long insertUser(Context context, Identity identity) {
        return insertUser(
                context,
                identity.getDisplayName(),
                identity.getAlias(),
                identity.getGroupKey(),
                identity.getPublicKey());
    }

    public static long insertUser(
            Context context,
            String displayName,
            String alias,
            Key groupKey,
            Key publicKey) {
        ContentValues userValues = new ContentValues();
        userValues.put(IslndContract.UserEntry.COLUMN_PUBLIC_KEY, CryptoUtil.encodeKey(publicKey));

        final ContentResolver contentResolver = context.getContentResolver();
        Uri result = contentResolver.insert(
                IslndContract.UserEntry.CONTENT_URI,
                userValues);
        long userId = ContentUris.parseId(result);

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

    public static Key getGroupKey(Context context, int userId) {
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

    public static Key getPublicKey(Context context, int userId) {
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
        contentResolver.delete(IslndContract.UserEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.ProfileEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.PostEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.CommentEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.AliasEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.DisplayNameEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.ReceivedEventEntry.CONTENT_URI, null, null);
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

    public static boolean containsPublicKey(Context context, Key publicKey) {
        String[] projection = new String[0];

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    IslndContract.UserEntry.COLUMN_PUBLIC_KEY + " = ?",
                    new String[] {CryptoUtil.encodeKey(publicKey)},
                    null);
            return cursor.moveToFirst();
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
}
