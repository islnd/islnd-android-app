package com.island.island.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.island.island.DeletePostFragment;
import com.island.island.Models.Comment;
import com.island.island.Models.CommentKey;

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
    private static final String COMMENT_ID = "COMMENT_ID";
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
                + COMMENT_ID + " TEXT, "
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

    public void insert(Comment comment, int postUserId, String postId) {
        insert(comment.getCommentUserId(),
                postUserId,
                comment.getCommentId(),
                postId,
                comment.getContent(),
                comment.getTimestamp());
    }

    public void insert(int commentUserId, int postUserId, CommentUpdate commentUpdate) {
        insert(commentUserId,
                postUserId,
                commentUpdate.getCommentId(),
                commentUpdate.getPostId(),
                commentUpdate.getContent(),
                commentUpdate.getTimestamp());
    }

    private void insert(
            int commentUserId,
            int postUserId,
            String commentId,
            String postId,
            String content,
            long timestamp) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COMMENT_USER_ID, commentUserId);
        values.put(POST_USER_ID, postUserId);
        values.put(COMMENT_ID, commentId);
        values.put(POST_ID, postId);
        values.put(CONTENT, content);
        values.put(TIMESTAMP, timestamp);
        Log.v(TAG, String.format("adding comment. commentuser: %d postuser %d. post id %s",
                        commentUserId, postUserId, postId));

        writableDatabase.insert(DATABASE_NAME, null, values);
    }

    public void delete(CommentKey commentKey) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        String selection = COMMENT_USER_ID + " = ? AND " + COMMENT_ID + " = ?";
        String[] args = {
                String.valueOf(commentKey.getCommentAuthorId()),
                commentKey.getCommentId()};
        Log.v(TAG, String.format("deleting comment: %s", commentKey));

        writableDatabase.delete(DATABASE_NAME, selection, args);
    }

    public List<Comment> getComments(int postUserId, String postId) {
        List<Comment> comments = new ArrayList<>();
        SQLiteDatabase readableDatabase = getReadableDatabase();

        String[] columns = {COMMENT_USER_ID, COMMENT_ID, CONTENT, TIMESTAMP};
        String query = POST_USER_ID + " = ? AND " + POST_ID + " = ?";
        String[] args = {String.valueOf(postUserId), postId};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, query, args, "", "", "");
        while(results.moveToNext()) {
            comments.add(new Comment(
                            postUserId,
                            postId,
                            results.getInt(0),
                            results.getString(1),
                            results.getString(2),
                            results.getLong(3)));
        }

        return comments;
    }

    public boolean contains(int commentAuthorId, String commentId) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String[] columns = {COMMENT_USER_ID, COMMENT_ID};
        String selection = COMMENT_USER_ID + " = ? AND "
                + COMMENT_ID + " = ?";
        String[] args = {String.valueOf(commentAuthorId), commentId};

        Cursor results = readableDatabase.query(DATABASE_NAME, columns, selection, args, "", "", "");
        return results.getCount() > 0;
    }

    public boolean contains(Comment comment) {
        return contains(comment.getCommentUserId(), comment.getCommentId());
    }

    public boolean contains(CommentKey commentKey) {
        return contains(commentKey.getCommentAuthorId(), commentKey.getCommentId());
    }
}
