package com.island.island;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import com.island.island.Adapters.PostAdapter;
import com.island.island.Database.IslndContract;

public class PostLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private final Context mContext;
    private final PostAdapter mAdapter;

    public PostLoader(Context context, PostAdapter postAdapter) {
        mContext = context;
        mAdapter = postAdapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                IslndContract.UserEntry.COLUMN_USERNAME,
                IslndContract.UserEntry.COLUMN_PSEUDONYM,
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry._ID,
                IslndContract.PostEntry.COLUMN_USER_ID,
                IslndContract.PostEntry.COLUMN_POST_ID,
                IslndContract.PostEntry.COLUMN_TIMESTAMP,
                IslndContract.PostEntry.COLUMN_CONTENT,
        };

        return new CursorLoader(
                mContext,
                IslndContract.PostEntry.CONTENT_URI,
                projection,
                null,
                null,
                IslndContract.PostEntry.COLUMN_TIMESTAMP + " DESC"
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
