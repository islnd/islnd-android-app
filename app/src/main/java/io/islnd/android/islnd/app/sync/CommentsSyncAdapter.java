package io.islnd.android.islnd.app.sync;

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

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.Comment;
import io.islnd.android.islnd.app.models.CommentKey;
import io.islnd.android.islnd.app.models.PostKey;
import io.islnd.android.islnd.app.util.Util;

import io.islnd.android.islnd.messaging.CommentCollection;
import io.islnd.android.islnd.messaging.CommentUpdate;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.crypto.EncryptedComment;
import io.islnd.android.islnd.messaging.crypto.InvalidSignatureException;
import io.islnd.android.islnd.messaging.server.CommentQuery;
import io.islnd.android.islnd.messaging.server.CommentQueryRequest;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = CommentsSyncAdapter.class.getSimpleName();

    private Context mContext;
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
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.v(TAG, "starting on perform sync");

        //--Get the posts
        String[] projection = {
                IslndContract.AliasEntry.COLUMN_ALIAS,
                IslndContract.AliasEntry.COLUMN_GROUP_KEY,
                IslndContract.PostEntry.COLUMN_POST_ID,
        };
        Cursor cursor = mContentResolver.query(
                IslndContract.PostEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        List<CommentQuery> commentQueries = buildCommentQueries(cursor);
        for (CommentQuery commentQuery : commentQueries) {
            Log.v(TAG, "comment query" + commentQuery.toString());
        }

        //--Run query
        CommentQueryRequest commentQueryRequest = new CommentQueryRequest(commentQueries);
        List<EncryptedComment> encryptedComments = Rest.getComments(commentQueryRequest, Util.getApiKey(getContext()));
        if (encryptedComments == null) {
            Log.d(TAG, "getting comments returned null!");
            return;
        }

        Log.v(TAG, String.format("comment service received %d comments", encryptedComments.size()));

        //--Decrypt the comments
        CommentCollection commentCollection = new CommentCollection();
        for (EncryptedComment encryptedComment : encryptedComments) {
            int postAuthorId = DataUtils.getUserIdFromAlias(
                    mContext,
                    encryptedComment.getPostAuthorPseudonym());
            Key groupKey = DataUtils.getGroupKey(mContext, postAuthorId);

            CommentUpdate commentUpdate = encryptedComment.decrypt(groupKey);
            int commentAuthorId = DataUtils.getUserIdFromAlias(
                    mContext,
                    commentUpdate.getCommentAuthorPseudonym());
            Key publicKey = DataUtils.getPublicKey(mContext, commentAuthorId);

            try {
                encryptedComment.decryptAndVerify(
                        groupKey,
                        publicKey);
            } catch (InvalidSignatureException e) {
                Log.d(TAG, "could not verify comment + " + commentUpdate);
                e.printStackTrace();
            }

            commentCollection.add(
                    postAuthorId,
                    commentAuthorId,
                    commentUpdate);
        }

        // Add new comments to content provider
        ContentValues[] values = convertCommentsToContentValues(commentCollection);
        mContentResolver.bulkInsert(
                IslndContract.CommentEntry.CONTENT_URI,
                values);

        // Delete comments from content provider
        for (PostKey postKey :commentCollection.getDeletions().keySet()) {
            for (CommentKey commentKey : commentCollection.getDeletions().get(postKey)) {
                DataUtils.deleteComment(mContentResolver, commentKey);
            }
        }

        Log.v(TAG, "completed on perform sync");
    }

    @NonNull
    private List<CommentQuery> buildCommentQueries(Cursor cursor) {
        List<CommentQuery> commentQueries = new ArrayList<>();
        if (!cursor.moveToFirst()) {
            return commentQueries;
        }

        do {
            final String postAuthorPseudonym =
                    cursor.getString(cursor.getColumnIndex(IslndContract.AliasEntry.COLUMN_ALIAS));
            commentQueries.add(
                    new CommentQuery(
                            postAuthorPseudonym,
                            cursor.getString(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_POST_ID))
                    )
            );
        } while (cursor.moveToNext());

        return commentQueries;
    }

    private ContentValues[] convertCommentsToContentValues(CommentCollection commentCollection) {
        Map<PostKey, List<Comment>> commentUpdates = commentCollection.getCommentsGroupedByPostKey();
        List<ContentValues> setOfValues = new ArrayList<>();
        for (PostKey postKey : commentUpdates.keySet()) {
            for (Comment commentUpdate : commentUpdates.get(postKey)) {
                ContentValues values = buildContentValues(postKey, commentUpdate);
                setOfValues.add(values);
            }
        }

        ContentValues[] arrayOfValues = new ContentValues[setOfValues.size()];
        setOfValues.toArray(arrayOfValues);
        return arrayOfValues;
    }

    private ContentValues buildContentValues(PostKey postKey, Comment commentUpdate) {
        ContentValues values = new ContentValues();
        values.put(
                IslndContract.CommentEntry.COLUMN_POST_USER_ID,
                postKey.getUserId());
        values.put(
                IslndContract.CommentEntry.COLUMN_POST_ID,
                postKey.getPostId());
        values.put(
                IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID,
                commentUpdate.getCommentUserId());
        values.put(
                IslndContract.CommentEntry.COLUMN_COMMENT_ID,
                commentUpdate.getCommentId());
        values.put(
                IslndContract.CommentEntry.COLUMN_TIMESTAMP,
                commentUpdate.getTimestamp());
        values.put(
                IslndContract.CommentEntry.COLUMN_CONTENT,
                commentUpdate.getContent());

        return values;
    }
}
