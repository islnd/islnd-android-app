package io.islnd.android.islnd.app.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import io.islnd.android.islnd.app.activities.NavBaseActivity;
import io.islnd.android.islnd.app.models.CommentKey;
import io.islnd.android.islnd.app.models.PostKey;

import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.messaging.PseudonymKey;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

import java.security.Key;

public class DataUtils {
    public static long insertUser(Context context, PseudonymKey pk) {
        return insertUser(context, pk.getUsername(), pk.getPseudonym(), pk.getKey());
    }

    public static long insertUser(Context context, String username, String pseudonym, Key groupKey) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.UserEntry.COLUMN_PSEUDONYM, pseudonym);
        values.put(IslndContract.UserEntry.COLUMN_USERNAME, username);
        values.put(IslndContract.UserEntry.COLUMN_GROUP_KEY, CryptoUtil.encodeKey(groupKey));

        Uri result = context.getContentResolver().insert(
                IslndContract.UserEntry.CONTENT_URI,
                values);
        return ContentUris.parseId(result);
    }

    public static String getPseudonym(Context context, int userId) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_PSEUDONYM,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                IslndContract.UserEntry._ID + " = ?",
                new String[]{Integer.toString(userId)},
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

    public static String getPseudonym(Context context, String username) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_PSEUDONYM,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                IslndContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{username},
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

    public static int getUserId(Context context, String username) {
        String[] projection = new String[] {
                IslndContract.UserEntry._ID,
        };

        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    IslndContract.UserEntry.COLUMN_USERNAME + " = ?",
                    new String[] {username},
                    null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            else {
                return -1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Key getGroupKey(Context context, int userId) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_GROUP_KEY,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                IslndContract.UserEntry._ID + " = ?",
                new String[] {Integer.toString(userId)},
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

    public static Key getGroupKey(Context context, String username) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_GROUP_KEY,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                IslndContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[] {username},
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

    public static String getUsernameFromPseudonym(Context context, String pseudonym) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_USERNAME,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                IslndContract.UserEntry.COLUMN_PSEUDONYM + " = ?",
                new String[] {pseudonym},
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

    public static void deletePost(Context context, PostKey postKey) {
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

    public static Profile getProfile(Context context, String username) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_USERNAME,
                IslndContract.ProfileEntry.COLUMN_ABOUT_ME,
                IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI,
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI,
        };

        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(
                    IslndContract.ProfileEntry.CONTENT_URI,
                    projection,
                    IslndContract.UserEntry.COLUMN_USERNAME + " = ?",
                    new String[] {username},
                    null);
            if (cursor.moveToFirst()) {
                return new Profile(
                        cursor.getString(cursor.getColumnIndex(IslndContract.UserEntry.COLUMN_USERNAME)),
                        cursor.getString(cursor.getColumnIndex(IslndContract.ProfileEntry.COLUMN_ABOUT_ME)),
                        Uri.parse(cursor.getString(cursor.getColumnIndex(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI))),
                        Uri.parse(cursor.getString(cursor.getColumnIndex(IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI))),
                        1   //--Our database only holds one profile per user, so the version doesn't matter
                            //  The version is part of the profile when retrieving profiles from the network,
                            //  and we have to figure out which one is the most recent
                );
            }
            else {
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void updateProfile(Context applicationContext, Profile newProfile, int userId) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.ProfileEntry.COLUMN_ABOUT_ME, newProfile.getAboutMe());
        values.put(
                IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI,
                newProfile.getHeaderImageUri().toString());
        values.put(
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI,
                newProfile.getProfileImageUri().toString());
        final String selection = IslndContract.ProfileEntry.TABLE_NAME + "." +
                IslndContract.ProfileEntry.COLUMN_USER_ID + " = ?";
        applicationContext.getContentResolver().update(
                IslndContract.ProfileEntry.CONTENT_URI,
                values,
                selection,
                new String[]{Integer.toString(userId)}
        );
    }

    public static void deleteAll(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(IslndContract.UserEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.ProfileEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.PostEntry.CONTENT_URI, null, null);
        contentResolver.delete(IslndContract.CommentEntry.CONTENT_URI, null, null);
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
}
