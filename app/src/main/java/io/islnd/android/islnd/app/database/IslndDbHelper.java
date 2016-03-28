package io.islnd.android.islnd.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IslndDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 18;

    static final String DATABASE_NAME = "islnd.db";

    public IslndDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + IslndContract.UserEntry.TABLE_NAME + " (" +
                IslndContract.UserEntry._ID + " INTEGER PRIMARY KEY, " +
                IslndContract.UserEntry.COLUMN_PUBLIC_KEY + " TEXT NOT NULL, " +
                IslndContract.UserEntry.COLUMN_MESSAGE_INBOX + " TEXT NOT NULL, " +
                IslndContract.UserEntry.COLUMN_MESSAGE_OUTBOX + " TEXT NULL " +
                " );";

        final String SQL_CREATE_ALIAS_TABLE = "CREATE TABLE " + IslndContract.AliasEntry.TABLE_NAME + " (" +
                IslndContract.AliasEntry._ID + " INTEGER PRIMARY KEY, " +
                IslndContract.AliasEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                IslndContract.AliasEntry.COLUMN_ALIAS + " TEXT NOT NULL, " +
                IslndContract.AliasEntry.COLUMN_GROUP_KEY + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + IslndContract.AliasEntry.COLUMN_USER_ID + ") REFERENCES " +
                IslndContract.UserEntry.TABLE_NAME + " (" + IslndContract.UserEntry._ID + "), " +

                " UNIQUE (" + IslndContract.AliasEntry.COLUMN_USER_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_DISPLAY_NAME_TABLE = "CREATE TABLE " + IslndContract.DisplayNameEntry.TABLE_NAME + " (" +
                IslndContract.DisplayNameEntry._ID + " INTEGER PRIMARY KEY, " +
                IslndContract.DisplayNameEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + IslndContract.DisplayNameEntry.COLUMN_USER_ID + ") REFERENCES " +
                IslndContract.UserEntry.TABLE_NAME + " (" + IslndContract.UserEntry._ID + "), " +

                " UNIQUE (" + IslndContract.DisplayNameEntry.COLUMN_USER_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_POST_TABLE = "CREATE TABLE " + IslndContract.PostEntry.TABLE_NAME + " (" +
                IslndContract.PostEntry._ID + " INTEGER PRIMARY KEY, " +
                IslndContract.PostEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                IslndContract.PostEntry.COLUMN_POST_ID + " TEXT NOT NULL, " +
                IslndContract.PostEntry.COLUMN_ALIAS + " TEXT NOT NULL, " +
                IslndContract.PostEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                IslndContract.PostEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                IslndContract.PostEntry.COLUMN_COMMENT_COUNT + " INTEGER NOT NULL, " +

                " FOREIGN KEY (" + IslndContract.PostEntry.COLUMN_USER_ID + ") REFERENCES " +
                IslndContract.UserEntry.TABLE_NAME + " (" + IslndContract.UserEntry._ID + "), " +

                " UNIQUE (" + IslndContract.PostEntry.COLUMN_USER_ID + ", " +
                IslndContract.PostEntry.COLUMN_POST_ID + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_COMMENT_TABLE = "CREATE TABLE " + IslndContract.CommentEntry.TABLE_NAME + " (" +
                IslndContract.CommentEntry._ID + " INTEGER PRIMARY KEY," +
                IslndContract.CommentEntry.COLUMN_POST_AUTHOR_ALIAS + " TEXT NOT NULL, " +
                IslndContract.CommentEntry.COLUMN_POST_ID + " TEXT NOT NULL, " +
                IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID + " INTEGER NOT NULL, " +
                IslndContract.CommentEntry.COLUMN_COMMENT_ID + " TEXT NOT NULL, " +
                IslndContract.CommentEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                IslndContract.CommentEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID + ") REFERENCES " +
                IslndContract.UserEntry.TABLE_NAME + " (" + IslndContract.UserEntry._ID + "), " +

                " UNIQUE (" + IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID + ", " +
                IslndContract.CommentEntry.COLUMN_COMMENT_ID + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_PROFILE_TABLE = "CREATE TABLE " + IslndContract.ProfileEntry.TABLE_NAME + " (" +
                IslndContract.ProfileEntry._ID + " INTEGER PRIMARY KEY," +
                IslndContract.ProfileEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                IslndContract.ProfileEntry.COLUMN_ABOUT_ME + " TEXT NOT NULL, " +
                IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI + " TEXT NOT NULL, " +
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + IslndContract.ProfileEntry.COLUMN_USER_ID + ") REFERENCES " +
                IslndContract.UserEntry.TABLE_NAME + " (" + IslndContract.UserEntry._ID + "), " +

                " UNIQUE (" + IslndContract.ProfileEntry.COLUMN_USER_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_RECEIVED_EVENT_TABLE = "CREATE TABLE " + IslndContract.ReceivedEventEntry.TABLE_NAME + " (" +
                IslndContract.ReceivedEventEntry._ID + " INTEGER PRIMARY KEY," +
                IslndContract.ReceivedEventEntry.COLUMN_ALIAS + " TEXT NOT NULL, " +
                IslndContract.ReceivedEventEntry.COLUMN_EVENT_ID + " INTEGER NOT NULL, " +

                " UNIQUE (" + IslndContract.ReceivedEventEntry.COLUMN_ALIAS +", " +
                IslndContract.ReceivedEventEntry.COLUMN_EVENT_ID + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_OUTGOING_EVENT_TABLE = "CREATE TABLE " + IslndContract.OutgoingEventEntry.TABLE_NAME + " (" +
                IslndContract.OutgoingEventEntry._ID + " INTEGER PRIMARY KEY," +
                IslndContract.OutgoingEventEntry.COLUMN_ALIAS + " TEXT NOT NULL, " +
                IslndContract.OutgoingEventEntry.COLUMN_BLOB + " INTEGER NOT NULL);";

        final String SQL_CREATE_MAILBOX_TABLE = "CREATE TABLE " + IslndContract.MailboxEntry.TABLE_NAME + " (" +
                IslndContract.MailboxEntry._ID + " INTEGER PRIMARY KEY," +
                IslndContract.MailboxEntry.COLUMN_MAILBOX + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_ALIAS_TABLE);
        db.execSQL(SQL_CREATE_DISPLAY_NAME_TABLE);
        db.execSQL(SQL_CREATE_POST_TABLE);
        db.execSQL(SQL_CREATE_COMMENT_TABLE);
        db.execSQL(SQL_CREATE_PROFILE_TABLE);
        db.execSQL(SQL_CREATE_RECEIVED_EVENT_TABLE);
        db.execSQL(SQL_CREATE_OUTGOING_EVENT_TABLE);
        db.execSQL(SQL_CREATE_MAILBOX_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + IslndContract.UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + IslndContract.AliasEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + IslndContract.DisplayNameEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + IslndContract.PostEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + IslndContract.CommentEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + IslndContract.ProfileEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + IslndContract.ReceivedEventEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + IslndContract.OutgoingEventEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + IslndContract.MailboxEntry.TABLE_NAME);
        onCreate(db);
    }
}
