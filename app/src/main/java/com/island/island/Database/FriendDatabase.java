package com.island.island.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.island.messaging.crypto.CryptoUtil;
import org.island.messaging.PseudonymKey;

import java.util.ArrayList;

/*
    When we add support for unfriending and changing pseudonyms,
    this table will become two tables. The two tables will allow
    a constant user id and public key, which the pseudonym and group
    key can change.
 */
public class FriendDatabase extends SQLiteOpenHelper {

    private static final String TAG = FriendDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "FRIEND_DATABASE";
    private static final int DATABASE_VERSION = 1;
    private static FriendDatabase SINGLE = null;

    private static final String ID = "ID";
    private static final String USER_NAME = "USER_NAME";
    private static final String PSEUDONYM = "PSEUDONYM";
    private static final String GROUP_KEY = "GROUP_KEY";

    private FriendDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DATABASE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_NAME + " TEXT, "
                + PSEUDONYM + " TEXT, "
                + GROUP_KEY + " TEXT)";
        Log.v(TAG, String.format("Creating database %s", DATABASE_NAME));
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static FriendDatabase getInstance(Context context) {
        if (SINGLE == null) {
            SINGLE = new FriendDatabase(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        return SINGLE;
    }

    public void addFriend(PseudonymKey pseudonymKey) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_NAME, pseudonymKey.getUsername());
        values.put(PSEUDONYM, pseudonymKey.getPseudonym());
        values.put(GROUP_KEY, CryptoUtil.encodeKey(pseudonymKey.getKey()));
        Log.v(TAG, "adding friend w username " + pseudonymKey.getUsername());

        writableDatabase.insert(DATABASE_NAME, null, values);
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DATABASE_NAME, null, null);
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

    public ArrayList<PseudonymKey> getKeys() {
        ArrayList<PseudonymKey> keys = new ArrayList<>();
        SQLiteDatabase readableDatabase = getReadableDatabase();

        String[] columns = {USER_NAME, PSEUDONYM, GROUP_KEY};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, null, null, "", "", "");
        while(results.moveToNext()) {
            keys.add(new PseudonymKey(1, results.getString(0), results.getString(1),
                    CryptoUtil.decodeSymmetricKey(results.getString(2))));
        }

        return keys;
    }

    public PseudonymKey getKey(String username) {
        SQLiteDatabase readableDatabase = getReadableDatabase();

        String[] columns = {USER_NAME, PSEUDONYM, GROUP_KEY};
        String selection = USER_NAME + " = ?";
        String[] args = {username};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, selection, args, "", "", "");
        if (results.getCount() == 0) {
            return null;
        }

        results.moveToNext();
        return new PseudonymKey(
                1, //--TODO PK ID
                results.getString(0),
                results.getString(1),
                CryptoUtil.decodeSymmetricKey(results.getString(2)));
    }

    public int getUserId(String username) {
        SQLiteDatabase readableDatabase = getReadableDatabase();

        String[] columns = {ID, USER_NAME};
        String selection = USER_NAME + " = ?";
        String[] args = {username};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, selection, args, "", "", "");
        if (results.getCount() == 0) {
            return -1;
        }

        results.moveToNext();
        return results.getInt(0);
    }

    public String getUsername(int userId) {
        SQLiteDatabase readableDatabase = getReadableDatabase();

        String[] columns = {ID, USER_NAME};
        String selection = ID + " = ?";
        String[] args = {String.valueOf(userId)};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, selection, args, "", "", "");
        if (results.getCount() == 0) {
            return "";
        }

        results.moveToNext();
        return results.getString(1);
    }
}
