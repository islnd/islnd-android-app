package com.island.island.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.island.island.Models.Post;
import com.island.island.Models.RawPost;

import org.island.messaging.PostUpdate;

import java.util.ArrayList;
import java.util.List;

public class PostDatabase extends SQLiteOpenHelper {
    private static final String TAG = PostDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "POST_DATABASE";
    private static final int DATABASE_VERSION = 1;
    private static PostDatabase SINGLE = null;

    private static final String USER_ID = "USER_ID";
    private static final String POST_ID = "POST_ID";
    private static final String CONTENT = "CONTENT";
    private static final String TIMESTAMP = "TIMESTAMP";

    private PostDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DATABASE_NAME + " ("
                + USER_ID + " INT, "
                + POST_ID + " TEXT, "
                + CONTENT + " TEXT, "
                + TIMESTAMP + " INT)";
        Log.v(TAG, String.format("Creating database %s", DATABASE_NAME));
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static PostDatabase getInstance(Context context) {
        if (SINGLE == null) {
            SINGLE = new PostDatabase(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        return SINGLE;
    }

    public void insert(int userId, PostUpdate postUpdate) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_ID, userId);
        values.put(POST_ID, postUpdate.getId());
        values.put(CONTENT, postUpdate.getContent());
        values.put(TIMESTAMP, postUpdate.getTimestamp());
        Log.v(TAG, String.format("adding post. user id %d. post id %s", userId, postUpdate.getId()));

        writableDatabase.insert(DATABASE_NAME, null, values);
    }

    public void insert(int userId, Post post) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_ID, userId);
        values.put(POST_ID, post.getPostId());
        values.put(CONTENT, post.getContent());
        values.put(TIMESTAMP, post.getTimestamp());
        Log.v(TAG, String.format("adding post. user id %d. post id %s", userId, post.getPostId()));

        writableDatabase.insert(DATABASE_NAME, null, values);
    }

    public List<RawPost> getAll() {
        List<RawPost> posts = new ArrayList<>();
        SQLiteDatabase readableDatabase = getReadableDatabase();

        String[] columns = {USER_ID, POST_ID, CONTENT, TIMESTAMP};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, null, null, "", "", "");
        while(results.moveToNext()) {
            posts.add(new RawPost(
                            results.getInt(0),
                            results.getString(1),
                            results.getString(2),
                            results.getLong(3)
                    ));
        }

        return posts;
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DATABASE_NAME, null, null);
    }

    public boolean contains(int userId, Post post) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String[] columns = {USER_ID, TIMESTAMP};
        String selection = USER_ID + " = ? AND " + TIMESTAMP + " = ?";
        String[] args = {String.valueOf(userId), String.valueOf(post.getTimestamp())};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, selection, args, "", "", "");
        return results.getCount() > 0;
    }

    public boolean contains(int userId, String postId) {
        SQLiteDatabase readableDatabase = getReadableDatabase();
        String[] columns = {USER_ID, POST_ID};
        String selection = USER_ID + " = ? AND " + POST_ID + " = ?";
        String[] args = {String.valueOf(userId), postId};
        Cursor results = readableDatabase.query(DATABASE_NAME, columns, selection, args, "", "", "");
        return results.getCount() > 0;
    }

    public void delete(int userId, String postId) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = USER_ID + " = ? AND " + POST_ID + " = ?";
        String[] args = {String.valueOf(userId), postId};
        Log.v(TAG, String.format("deleting post. user id %d. post id %s", userId, postId));

        db.delete(DATABASE_NAME, selection, args);
    }
}
