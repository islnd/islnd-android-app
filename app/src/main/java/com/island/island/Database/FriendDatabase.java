package com.island.island.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.island.messaging.Crypto;
import org.island.messaging.PseudonymKey;

public class FriendDatabase extends SQLiteOpenHelper {

    private static final String TAG = FriendDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "FRIEND_DATABASE";
    private static final int DATABASE_VERSION = 1;
    private static FriendDatabase SINGLE = null;

    private static final String USER_NAME = "USER_NAME";
    private static final String PSEUDONYM = "PSEUDONYM";

    private FriendDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static FriendDatabase getInstance(Context context) {
        if (SINGLE == null) {
            SINGLE = new FriendDatabase(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        return SINGLE;
    }

    public void addFriend(PseudonymKey pseudonymKey) {
        SQLiteDatabase writeableDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_NAME, pseudonymKey.getUsername());
        values.put(PSEUDONYM, pseudonymKey.getPseudonym());
        values.put("GROUP_KEY", Crypto.encodeKey(pseudonymKey.getKey()));

        writeableDatabase.insert(DATABASE_NAME, null, values);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DATABASE_NAME
                + " (" + USER_NAME + " TEXT, " + PSEUDONYM + " TEXT, GROUP_KEY TEXT)";
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

    public int getRows() {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String[] columns = {USER_NAME};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, "", null, "", "", "");
        return results.getCount();
    }

    public boolean contains(PseudonymKey pseudonymKey) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String[] columns = {USER_NAME, PSEUDONYM};
        String selection = USER_NAME + " = ? AND " + PSEUDONYM + " = ?";
        String[] args = {pseudonymKey.getUsername(), pseudonymKey.getPseudonym()};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, selection, args, "", "", "");
        return results.getCount() > 0;
    }
}
