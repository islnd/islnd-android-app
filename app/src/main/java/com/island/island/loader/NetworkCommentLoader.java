package com.island.island.loader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.island.island.Database.IslndContract;
import com.island.island.Models.Comment;
import com.island.island.Models.PostKey;

import org.island.messaging.CommentCollection;
import org.island.messaging.MessageLayer;

import java.util.List;

public class NetworkCommentLoader extends AsyncTaskLoader<Void> {

    private final int postUserId;
    private final String postId;
    private final Context mContext;
    private final ContentResolver mContentResolver;

    public NetworkCommentLoader(Context context, int postUserId, String postId) {
        super(context);
        this.postUserId = postUserId;
        this.postId = postId;
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    @Override
    public Void loadInBackground() {
        CommentCollection commentCollection = MessageLayer.getCommentCollection(
                mContext,
                postUserId,
                postId);

        ContentValues[] values =
                convertCommentsToContentValues(
                        commentCollection.getCommentsGroupedByPostKey().get(new PostKey(postUserId, postId)),
                        postUserId,
                        postId);

        mContentResolver.bulkInsert(
                IslndContract.CommentEntry.CONTENT_URI,
                values);

        return null;
    }

    private ContentValues[] convertCommentsToContentValues(List<Comment> commentUpdates, int postUserId, String postId) {
        ContentValues[] values = new ContentValues[commentUpdates.size()];
        for (int i = 0; i < commentUpdates.size(); i++) {
            Comment commentUpdate = commentUpdates.get(i);

            values[i] = new ContentValues();
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_POST_USER_ID,
                    postUserId);
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_POST_ID,
                    postId);
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID,
                    commentUpdate.getCommentUserId());
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
}
