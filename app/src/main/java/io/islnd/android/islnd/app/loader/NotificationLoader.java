package io.islnd.android.islnd.app.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import io.islnd.android.islnd.app.adapters.NotificationAdapter;
import io.islnd.android.islnd.app.database.IslndContract;

public class NotificationLoader implements LoaderManager.LoaderCallbacks<Cursor>{

    private final Context mContext;
    private final Uri mContentUri;
    private final NotificationAdapter mAdapter;

    public NotificationLoader(Context context, Uri contentUri, NotificationAdapter notificationAdapter) {
        mContext = context;
        mAdapter = notificationAdapter;
        mContentUri = contentUri;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry._ID,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_NOTIFICATION_USER_ID,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_NOTIFICATION_TYPE,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_POST_ID,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_TIMESTAMP,
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME,
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI
        };

        return new CursorLoader(
                mContext,
                mContentUri,
                projection,
                null,
                null,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_TIMESTAMP + " DESC"
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
