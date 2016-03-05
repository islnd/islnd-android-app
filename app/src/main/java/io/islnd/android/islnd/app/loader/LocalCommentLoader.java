package io.islnd.android.islnd.app.loader;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import io.islnd.android.islnd.app.adapters.ViewPostAdapter;
import io.islnd.android.islnd.app.database.IslndContract;

public class LocalCommentLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String POST_AUTHOR_ID_BUNDLE_KEY = "post_author_bundle_key";
    public static final String POST_ID_BUNDLE_KEY = "post_id_bundle_key";

    private final Context mContext;
    private final ViewPostAdapter mAdapter;

    public LocalCommentLoader(Context context, ViewPostAdapter adapter) {
        mContext = context;
        mAdapter = adapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                IslndContract.UserEntry.COLUMN_USERNAME,
                IslndContract.CommentEntry.TABLE_NAME + "." + IslndContract.CommentEntry._ID,
                IslndContract.CommentEntry.COLUMN_POST_USER_ID,
                IslndContract.CommentEntry.COLUMN_POST_ID,
                IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID,
                IslndContract.CommentEntry.COLUMN_COMMENT_ID,
                IslndContract.CommentEntry.COLUMN_TIMESTAMP,
                IslndContract.CommentEntry.COLUMN_CONTENT,
        };

        int postAuthorId = args.getInt(POST_AUTHOR_ID_BUNDLE_KEY);
        String postId = args.getString(POST_ID_BUNDLE_KEY);
        return new CursorLoader(
                mContext,
                IslndContract.CommentEntry.buildCommentUriWithPostAuthorIdAndPostId(postAuthorId, postId),
                projection,
                null,
                null,
                IslndContract.CommentEntry.COLUMN_TIMESTAMP + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
