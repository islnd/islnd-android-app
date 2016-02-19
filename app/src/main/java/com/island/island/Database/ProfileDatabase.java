package com.island.island.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.island.island.Models.Profile;

public class ProfileDatabase extends SQLiteOpenHelper {

    private static final String TAG = ProfileDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "PROFILE_DATABASE";
    private static final int DATABASE_VERSION = 1;
    private static ProfileDatabase SINGLE = null;

    private static final String USER_NAME = "USER_NAME";
    private static final String ABOUT_ME = "ABOUT_ME";

    private ProfileDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static ProfileDatabase getInstance(Context context) {
        if (SINGLE == null) {
            SINGLE = new ProfileDatabase(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        return SINGLE;
    }

    public void insert(Profile profile) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_NAME, profile.getUsername());
        values.put(ABOUT_ME, profile.getAboutMe());
        Log.v(TAG, "adding profile for " + profile.getUsername());

        writableDatabase.insert(DATABASE_NAME, null, values);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DATABASE_NAME
                + " (" + USER_NAME + " TEXT, " + ABOUT_ME + " TEXT)";
        Log.v(TAG, String.format("Creating database %s", DATABASE_NAME));
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteAll() {
        SQLiteDatabase delete = getWritableDatabase();
        delete.delete(DATABASE_NAME, null, null);
    }

    public Profile get(String username) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String[] columns = {USER_NAME, ABOUT_ME};
        String selection = USER_NAME + " = ?";
        String[] args = {username};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, selection, args, "", "", "");
        if (results.getCount() == 0) {
            return null;
        }

        results.moveToNext();
        return new Profile(results.getString(0), results.getString(1), Integer.MIN_VALUE);
    }

    public void update(Profile profile) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.delete(
                DATABASE_NAME,
                USER_NAME + " = ?",
                new String[]{profile.getUsername()});

        insert(profile);
    }

    public boolean hasProfile(Profile profile) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String[] columns = {USER_NAME};
        String selection = USER_NAME + " = ?";
        String[] args = { profile.getUsername() };
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, selection, args, "", "", "");
        return results.getCount() > 0;
    }
}

