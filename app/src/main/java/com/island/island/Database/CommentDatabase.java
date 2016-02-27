package com.island.island.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.island.island.Models.Comment;

import org.island.messaging.CommentUpdate;

import java.util.ArrayList;
import java.util.List;

public class CommentDatabase extends SQLiteOpenHelper {
    private static final String TAG = CommentDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "COMMENT_DATABASE";
    private static final int DATABASE_VERSION = 1;
    private static CommentDatabase SINGLE = null;

    private static final String COMMENT_USER_ID = "COMMENT_USER_ID";
    private static final String POST_USER_ID = "POST_USER_ID";
    private static final String POST_ID = "POST_ID";
    private static final String CONTENT = "CONTENT";
    private static final String TIMESTAMP = "TIMESTAMP";

    private CommentDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DATABASE_NAME + " ("
                + COMMENT_USER_ID + " INT, "
                + POST_USER_ID + " INT, "
                + POST_ID + " TEXT, "
                + CONTENT + " TEXT, "
                + TIMESTAMP + " INT)";
        Log.v(TAG, String.format("Creating database %s", DATABASE_NAME));
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static CommentDatabase getInstance(Context context) {
        if (SINGLE == null) {
            SINGLE = new CommentDatabase(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        return SINGLE;
    }

    public void insert(int commentUserId, int postUserId, CommentUpdate commentUpdate) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COMMENT_USER_ID, commentUserId);
        values.put(POST_USER_ID, postUserId);
        values.put(POST_ID, commentUpdate.getPostId());
        values.put(CONTENT, commentUpdate.getContent());
        values.put(TIMESTAMP, commentUpdate.getTimestamp());
        Log.v(TAG, String.format("adding comment. commentuser: %d postuser %d. post id %s", commentUserId, postUserId, commentUpdate.getPostId()));

        writableDatabase.insert(DATABASE_NAME, null, values);
    }

    public List<Comment> getComments(int postUserId, String postId) {
        List<Comment> comments = new ArrayList<>();
        SQLiteDatabase readableDatabase = getReadableDatabase();

        String[] columns = {COMMENT_USER_ID, CONTENT, TIMESTAMP};
        String query = POST_USER_ID + " = ? AND " + POST_ID + " = ?";
        String[] args = {String.valueOf(postUserId), postId};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, query, args, "", "", "");
        while(results.moveToNext()) {
            comments.add(new Comment(
                            postUserId,
                            postId,
                            results.getInt(0),
                            results.getString(1),
                            results.getLong(2)));
        }

        return comments;
    }

    public boolean contains(int postAuthorId, int commentAuthorId, long timestamp) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String[] columns = {POST_USER_ID, COMMENT_USER_ID, TIMESTAMP};
        String selection = POST_USER_ID + " = ? AND "
                + COMMENT_USER_ID + " = ? AND "
                + TIMESTAMP + " = ?";
        String[] args = {String.valueOf(postAuthorId),
                String.valueOf(commentAuthorId),
                String.valueOf(timestamp) };
        
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, selection, args, "", "", "");
        return results.getCount() > 0;
    }
}
