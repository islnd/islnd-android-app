package com.island.island.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.island.island.Database.IslndContract.PostEntry;

public class IslndDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "islnd.db";

    public IslndDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_POST_TABLE = "CREATE TABLE " + PostEntry.TABLE_NAME + " (" +
                PostEntry._ID + " INTEGER PRIMARY KEY," +
                PostEntry.COLUMN_USER_ID + " INTEGER NOT NULL," +
                PostEntry.COLUMN_POST_ID + " TEXT NOT NULL," +
                PostEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL," +
                PostEntry.COLUMN_CONTENT + " TEXT NOT NULL" +
                " );";

        db.execSQL(SQL_CREATE_POST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PostEntry.TABLE_NAME);
        onCreate(db);
    }
}
