package io.islnd.android.islnd.app.loader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.Comment;
import io.islnd.android.islnd.app.models.CommentKey;
import io.islnd.android.islnd.app.models.PostKey;

import io.islnd.android.islnd.messaging.CommentCollection;
import io.islnd.android.islnd.messaging.MessageLayer;

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

        final PostKey postKey = new PostKey(postUserId, postId);
        ContentValues[] values =
                convertCommentsToContentValues(
                        commentCollection.getCommentsGroupedByPostKey().get(
                                postKey),
                        postUserId,
                        postId);

        mContentResolver.bulkInsert(
                IslndContract.CommentEntry.CONTENT_URI,
                values);

        if (commentCollection.getDeletions() == null
                || commentCollection.getDeletions().get(postKey) == null) {
            return null;
        }

        for (CommentKey commentKeyToDelete : commentCollection.getDeletions().get(postKey)) {
            DataUtils.deleteComment(mContext, commentKeyToDelete);
        }

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
