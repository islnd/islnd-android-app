package com.island.island.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.island.island.Activities.NavBaseActivity;

import org.island.messaging.PseudonymKey;
import org.island.messaging.crypto.CryptoUtil;

import java.security.Key;

public class DataUtils {
    public static void insertUser(Context context, PseudonymKey pk) {
        insertUser(context, pk.getUsername(), pk.getPseudonym(), pk.getKey());
    }

    public static void insertUser(Context context, String username, String pseudonym, Key groupKey) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.UserEntry.COLUMN_PSEUDONYM, pseudonym);
        values.put(IslndContract.UserEntry.COLUMN_USERNAME, username);
        values.put(IslndContract.UserEntry.COLUMN_GROUP_KEY, CryptoUtil.encodeKey(groupKey));

        context.getContentResolver().insert(
                IslndContract.UserEntry.CONTENT_URI,
                values);
    }

    public static String getPseudonym(Context context, int userId) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_PSEUDONYM,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                IslndContract.UserEntry._ID + " = ?",
                new String[] {Integer.toString(userId)},
                null);

        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }

        return null;
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

    public static int deleteUsers(Context context) {
        return context.getContentResolver().delete(
                IslndContract.UserEntry.CONTENT_URI,
                null,
                null
        );
    }
}
