package io.islnd.android.islnd.app.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import io.islnd.android.islnd.app.adapters.PostAdapter;
import io.islnd.android.islnd.app.database.IslndContract;

public class LocalPostLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private final Context mContext;
    private final Uri mContentUri;
    private final PostAdapter mAdapter;

    public LocalPostLoader(Context context, Uri contentUri, PostAdapter postAdapter) {
        mContext = context;
        mAdapter = postAdapter;
        mContentUri = contentUri;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry._ID,
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_USER_ID,
                IslndContract.PostEntry.COLUMN_POST_ID,
                IslndContract.PostEntry.COLUMN_TIMESTAMP,
                IslndContract.PostEntry.COLUMN_CONTENT,
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME,
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI
        };

        return new CursorLoader(
                mContext,
                mContentUri,
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
