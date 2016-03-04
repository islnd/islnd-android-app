package com.island.island.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.island.island.Database.IslndContract.PostEntry;
import com.island.island.Database.IslndContract.UserEntry;
import com.island.island.Database.IslndContract.CommentEntry;

public class IslndDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;

    static final String DATABASE_NAME = "islnd.db";

    public IslndDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID + " INTEGER PRIMARY KEY," +
                UserEntry.COLUMN_USERNAME + " TEXT NOT NULL," +
                UserEntry.COLUMN_PSEUDONYM + " TEXT NOT NULL," +
                UserEntry.COLUMN_GROUP_KEY + " TEXT NOT NULL" +
                " );";

        final String SQL_CREATE_POST_TABLE = "CREATE TABLE " + PostEntry.TABLE_NAME + " (" +
                PostEntry._ID + " INTEGER PRIMARY KEY," +
                PostEntry.COLUMN_USER_ID + " INTEGER NOT NULL," +
                PostEntry.COLUMN_POST_ID + " TEXT NOT NULL, " +
                PostEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                PostEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + PostEntry.COLUMN_USER_ID + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry._ID + "), " +

                " UNIQUE (" + PostEntry.COLUMN_USER_ID + ", " +
                PostEntry.COLUMN_POST_ID + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_COMMENT_TABLE = "CREATE TABLE " + CommentEntry.TABLE_NAME + " (" +
                CommentEntry._ID + " INTEGER PRIMARY KEY," +
                CommentEntry.COLUMN_POST_USER_ID + " INTEGER NOT NULL, " +
                CommentEntry.COLUMN_POST_ID + " TEXT NOT NULL, " +
                CommentEntry.COLUMN_COMMENT_USER_ID + " INTEGER NOT NULL, " +
                CommentEntry.COLUMN_COMMENT_ID + " TEXT NOT NULL, " +
                CommentEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                CommentEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + CommentEntry.COLUMN_POST_USER_ID + ", " +
                CommentEntry.COLUMN_POST_ID + ") REFERENCES " +
                PostEntry.TABLE_NAME + " (" + PostEntry.COLUMN_USER_ID + ", " +
                PostEntry.COLUMN_POST_ID + "), " +

                " UNIQUE (" + CommentEntry.COLUMN_COMMENT_USER_ID + ", " +
                CommentEntry.COLUMN_COMMENT_ID + ") ON CONFLICT IGNORE);";

        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_POST_TABLE);
        db.execSQL(SQL_CREATE_COMMENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PostEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommentEntry.TABLE_NAME);
        onCreate(db);
    }
}