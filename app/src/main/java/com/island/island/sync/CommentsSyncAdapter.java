package com.island.island.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.island.island.Database.IslndContract;
import com.island.island.Utils.Utils;

import org.island.messaging.CommentUpdate;
import org.island.messaging.Rest;
import org.island.messaging.crypto.CryptoUtil;
import org.island.messaging.crypto.EncryptedComment;
import org.island.messaging.server.CommentQuery;
import org.island.messaging.server.CommentQueryRequest;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = CommentsSyncAdapter.class.getSimpleName();
    private Map<String, Integer> pseudonymToUserId;
    private Map<String, String> pseudonymToGroupKey;
    private ContentResolver mContentResolver;

    public CommentsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        init(context);
    }

    public CommentsSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        init(context);
    }

    private void init(Context context) {
        mContentResolver = context.getContentResolver();
        pseudonymToUserId = new HashMap<>();
        pseudonymToGroupKey = new HashMap<>();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.v(TAG, "starting on perform sync");
        buildPseudonymToUserIdMap();

        //--Get the posts
        String[] projection = {
                IslndContract.UserEntry.COLUMN_PSEUDONYM,
                IslndContract.UserEntry.COLUMN_GROUP_KEY,
                IslndContract.PostEntry.COLUMN_POST_ID,
        };
        Cursor cursor = mContentResolver.query(
                IslndContract.PostEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        buildPseudonymToGroupKeyMap(cursor);
        List<CommentQuery> commentQueries = buildCommentQueries(cursor);
        for (CommentQuery commentQuery : commentQueries) {
            Log.v(TAG, commentQuery.toString());
        }

        //--Run query
        CommentQueryRequest commentQueryRequest = new CommentQueryRequest(commentQueries);
        List<EncryptedComment> encryptedComments = Rest.getComments(commentQueryRequest, Utils.getApiKey(getContext()));
        if (encryptedComments == null) {
            Log.d(TAG, "getting comments returned null!");
            return;
        }

        Log.v(TAG, String.format("comment service received %d comments", encryptedComments.size()));

        //--Decrypt the comments
        List<CommentUpdate> commentUpdates = new ArrayList<>();
        for (EncryptedComment encryptedComment : encryptedComments) {
            CommentUpdate commentUpdate = encryptedComment.decrypt(getKey(encryptedComment.getPostAuthorPseudonym()));
            commentUpdates.add(commentUpdate);
        }

        //--save the comments
        ContentValues[] values = convertCommentsToContentValues(commentUpdates);
        mContentResolver.bulkInsert(
                IslndContract.CommentEntry.CONTENT_URI,
                values);

        Log.v(TAG, "completed on perform sync");
    }

    private Key getKey(String postAuthorPseudonym) {
        return CryptoUtil.decodeSymmetricKey(pseudonymToGroupKey.get(postAuthorPseudonym));
    }

    private void buildPseudonymToGroupKeyMap(Cursor cursor) {
        if (cursor == null
                || !cursor.moveToFirst()) {
            return;
        }

        do {
            final String pseudonym =
                    cursor.getString(cursor.getColumnIndex(IslndContract.UserEntry.COLUMN_PSEUDONYM));
            final String groupKey =
                    cursor.getString(cursor.getColumnIndex(IslndContract.UserEntry.COLUMN_GROUP_KEY));
            pseudonymToGroupKey.put(pseudonym, groupKey);
        } while (cursor.moveToNext());
    }

    @NonNull
    private List<CommentQuery> buildCommentQueries(Cursor cursor) {
        List<CommentQuery> commentQueries = new ArrayList<>();
        if (!cursor.moveToFirst()) {
            return commentQueries;
        }

        do {
            final String postAuthorPseudonym =
                    cursor.getString(cursor.getColumnIndex(IslndContract.UserEntry.COLUMN_PSEUDONYM));
            commentQueries.add(
                    new CommentQuery(
                            postAuthorPseudonym,
                            cursor.getString(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_POST_ID))
                    )
            );
        } while (cursor.moveToNext());

        return commentQueries;
    }

    @NonNull
    private ContentValues[] convertCommentsToContentValues(List<CommentUpdate> commentUpdates) {
        ContentValues[] values = new ContentValues[commentUpdates.size()];
        for (int i = 0; i < commentUpdates.size(); i++) {
            CommentUpdate commentUpdate = commentUpdates.get(i);
            if (pseudonymToUserId.get(commentUpdate.getPostAuthorPseudonym()) == null) {
                Log.d(TAG, "can't find pseudonym " + commentUpdate.getPostAuthorPseudonym());
            }

            values[i] = new ContentValues();
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_POST_USER_ID,
                    pseudonymToUserId.get(commentUpdate.getPostAuthorPseudonym()));
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_POST_ID,
                    commentUpdate.getPostId());
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID,
                    pseudonymToUserId.get(commentUpdate.getCommentAuthorPseudonym()));
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_COMMENT_ID,
                    commentUpdate.getCommentId());
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_TIMESTAMP,
                    commentUpdate.getTimestamp());
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_CONTENT,
                    commentUpdate.getContent());
        }

        return values;
    }

    private void buildPseudonymToUserIdMap() {
        String[] projection = {
                IslndContract.UserEntry._ID,
                IslndContract.UserEntry.COLUMN_PSEUDONYM,
                IslndContract.UserEntry.COLUMN_USERNAME,
        };
        Cursor cursor = mContentResolver.query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        if (cursor == null
                || !cursor.moveToFirst()) {
            return;
        }

        do {
            final String pseudonym =
                    cursor.getString(cursor.getColumnIndex(IslndContract.UserEntry.COLUMN_PSEUDONYM));
            final int userId = cursor.getInt(cursor.getColumnIndex(IslndContract.UserEntry._ID));
            pseudonymToUserId.put(pseudonym, userId);
            Log.v(TAG, String.format("Adding user %s to map", cursor.getString(2)));
        } while (cursor.moveToNext());
    }
}
