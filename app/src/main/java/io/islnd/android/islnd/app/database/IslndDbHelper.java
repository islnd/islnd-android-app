package io.islnd.android.islnd.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IslndDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;

    static final String DATABASE_NAME = "islnd.db";

    public IslndDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + IslndContract.UserEntry.TABLE_NAME + " (" +
                IslndContract.UserEntry._ID + " INTEGER PRIMARY KEY," +
                IslndContract.UserEntry.COLUMN_USERNAME + " TEXT NOT NULL," +
                IslndContract.UserEntry.COLUMN_PSEUDONYM + " TEXT NOT NULL," +
                IslndContract.UserEntry.COLUMN_GROUP_KEY + " TEXT NOT NULL" +
                " );";

        final String SQL_CREATE_POST_TABLE = "CREATE TABLE " + IslndContract.PostEntry.TABLE_NAME + " (" +
                IslndContract.PostEntry._ID + " INTEGER PRIMARY KEY," +
                IslndContract.PostEntry.COLUMN_USER_ID + " INTEGER NOT NULL," +
                IslndContract.PostEntry.COLUMN_POST_ID + " TEXT NOT NULL, " +
                IslndContract.PostEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                IslndContract.PostEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + IslndContract.PostEntry.COLUMN_USER_ID + ") REFERENCES " +
                IslndContract.UserEntry.TABLE_NAME + " (" + IslndContract.UserEntry._ID + "), " +

                " UNIQUE (" + IslndContract.PostEntry.COLUMN_USER_ID + ", " +
                IslndContract.PostEntry.COLUMN_POST_ID + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_COMMENT_TABLE = "CREATE TABLE " + IslndContract.CommentEntry.TABLE_NAME + " (" +
                IslndContract.CommentEntry._ID + " INTEGER PRIMARY KEY," +
                IslndContract.CommentEntry.COLUMN_POST_USER_ID + " INTEGER NOT NULL, " +
                IslndContract.CommentEntry.COLUMN_POST_ID + " TEXT NOT NULL, " +
                IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID + " INTEGER NOT NULL, " +
                IslndContract.CommentEntry.COLUMN_COMMENT_ID + " TEXT NOT NULL, " +
                IslndContract.CommentEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                IslndContract.CommentEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + IslndContract.CommentEntry.COLUMN_POST_USER_ID + ", " +
                IslndContract.CommentEntry.COLUMN_POST_ID + ") REFERENCES " +
                IslndContract.PostEntry.TABLE_NAME + " (" + IslndContract.PostEntry.COLUMN_USER_ID + ", " +
                IslndContract.PostEntry.COLUMN_POST_ID + "), " +

                " UNIQUE (" + IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID + ", " +
                IslndContract.CommentEntry.COLUMN_COMMENT_ID + ") ON CONFLICT IGNORE);";

        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_POST_TABLE);
        db.execSQL(SQL_CREATE_COMMENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + IslndContract.UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + IslndContract.PostEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + IslndContract.CommentEntry.TABLE_NAME);
        onCreate(db);
    }
}
