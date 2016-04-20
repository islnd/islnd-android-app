package io.islnd.android.islnd.app.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import io.islnd.android.islnd.app.adapters.InviteAdapter;
import io.islnd.android.islnd.app.adapters.NotificationAdapter;
import io.islnd.android.islnd.app.database.IslndContract;

public class InviteLoader implements LoaderManager.LoaderCallbacks<Cursor>{

    private final Context mContext;
    private final Uri mContentUri;
    private final InviteAdapter mAdapter;

    public InviteLoader(Context context, Uri contentUri, InviteAdapter inviteAdapter) {
        mContext = context;
        mAdapter = inviteAdapter;
        mContentUri = contentUri;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                IslndContract.InviteEntry._ID,
                IslndContract.InviteEntry.COLUMN_DISPLAY_NAME,
                IslndContract.InviteEntry.COLUMN_PHONE_NUMBER
        };

        return new CursorLoader(
                mContext,
                mContentUri,
                projection,
                null,
                null,
                IslndContract.InviteEntry._ID + " DESC"
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
