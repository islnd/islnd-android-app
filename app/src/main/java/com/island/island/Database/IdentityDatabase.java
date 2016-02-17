package com.island.island.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.island.island.Utils.Utils;

import org.island.messaging.Crypto;

import java.security.Key;

/**
 * Created by poo on 2/13/16.
 */
public class IdentityDatabase extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Identity2";

    public IdentityDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DATABASE_NAME + " (USER_NAME TEXT, PUBLIC_KEY TEXT, PRIVATE_KEY TEXT)";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void setIdentity(Key publicKey, Key privateKey, String username)
    {
        ContentValues values = new ContentValues();
        values.put("PUBLIC_KEY", Crypto.encodeKey(publicKey));
        values.put("PRIVATE_KEY", Crypto.encodeKey(privateKey));
        values.put("USER_NAME", username);

        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.insert(DATABASE_NAME, null, values);
    }

    public String getUsername() {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String[] columns = {"USER_NAME"};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, "", null, "", "", "");

        results.moveToLast();
        return results.getString(0);
    }

    public Key getPrivateKey() {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String[] columns = {"PRIVATE_KEY"};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, "", null, "", "", "");

        results.moveToLast();
        return Crypto.decodePrivateKey(results.getString(0));
    }

    public Key getPublicKey() {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String[] columns = {"PUBLIC_KEY"};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, "", null, "", "", "");
        if (results.getCount() == 0) {
            return null;
        }

        results.moveToLast();
        return Crypto.decodePublicKey(results.getString(0));
    }
}
