package io.islnd.android.islnd.app.loader;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import io.islnd.android.islnd.app.adapters.ViewFriendsAdapter;
import io.islnd.android.islnd.app.database.IslndContract;

public class FriendLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String USER_ID_BUNDLE_KEY = "USER_ID";

    public static final int LOADER_ID = 1;

    private final Context mContext;
    private final ViewFriendsAdapter mAdapter;

    public FriendLoader(Context context, ViewFriendsAdapter adapter) {
        mContext = context;
        mAdapter = adapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                IslndContract.DisplayNameEntry.TABLE_NAME + "." + IslndContract.DisplayNameEntry._ID,
                IslndContract.DisplayNameEntry.TABLE_NAME + "." + IslndContract.DisplayNameEntry.COLUMN_USER_ID,
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME,
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI
        };

        int myUserId = args.getInt(USER_ID_BUNDLE_KEY);
        String selection = IslndContract.DisplayNameEntry.TABLE_NAME + "." +
                IslndContract.DisplayNameEntry.COLUMN_USER_ID + " != ?";
        String[] selectionArgs = new String[]{
                Integer.toString(myUserId)
        };

        return new CursorLoader(
                mContext,
                IslndContract.ProfileEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME + " ASC"
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
