package com.island.island.Activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.island.island.Adapters.PostAdapter;
import com.island.island.Database.IslndContract;
import com.island.island.DeletePostFragment;
import com.island.island.PostCollection;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;

import org.island.messaging.MessageLayer;

public class FeedActivity extends NavBaseActivity implements
        DeletePostFragment.NoticeDeletePostListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = FeedActivity.class.getSimpleName();

    private static final int NEW_POST_RESULT = 1;
    public static final int DELETE_POST_RESULT = 2;

    private static final int POST_LOADER_ID = 0;

    private RecyclerView mRecyclerView;
    private PostAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;
    private boolean mAdapterInitialized;
    private ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Feed posts setup
        mRecyclerView = (RecyclerView) findViewById(R.id.feed_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        getLoaderManager().initLoader(POST_LOADER_ID, null, this);

        // Swipe to refresh
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);

        mRefreshLayout.setOnRefreshListener(
                () -> {
                    //--TODO get new content from network
                    new GetPostsFromServerTask().execute();
                    mRefreshLayout.setRefreshing(false);
                });

        Account account = createSyncAccount(this);
        mResolver = getContentResolver();
        mResolver.setSyncAutomatically(
                account,
                getString(R.string.content_authority),
                true);
        mResolver.requestSync(account, IslndContract.CONTENT_AUTHORITY, new Bundle());
    }

    public void startNewPostActivity(View view) {
        Intent newPostIntent = new Intent(FeedActivity.this, NewPostActivity.class);
        startActivityForResult(newPostIntent, NEW_POST_RESULT);
    }

    @Override
    public void onDeletePostDialogPositiveClick(DialogFragment dialogFragment) {
        //--TODO fix deletes
//        Bundle args = dialogFragment.getArguments();
//        String postId = args.getString(DeletePostFragment.POST_ID_BUNDLE_KEY);
//        int postUserId = args.getInt(DeletePostFragment.USER_ID_BUNDLE_KEY);
//        final PostKey postKey = new PostKey(postUserId, postId);
//        removePostFromFeed(postKey);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(TAG, "onCreateLoader");
        if (id == POST_LOADER_ID) {
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
                    this,
                    IslndContract.PostEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    IslndContract.PostEntry.COLUMN_TIMESTAMP + " DESC"
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == POST_LOADER_ID) {
            setPostsOnAdapter(data);
        }
    }

    private void setPostsOnAdapter(Cursor data) {
        Log.v(TAG, "post load finished");
        if (mAdapterInitialized) {
            mAdapter.swapCursor(data);
        }
        else {
            mAdapterInitialized = true;
            mAdapter = new PostAdapter(this, data);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(TAG, "onLoaderReset");
        mAdapter.swapCursor(null);
    }

    private class GetPostsFromServerTask extends AsyncTask<Void, Void, PostCollection> {
        private final String TAG = GetPostsFromServerTask.class.getSimpleName();

        @Override
        protected PostCollection doInBackground(Void... params) {
            return MessageLayer.getPosts(getApplicationContext());
        }
    }

    public static Account createSyncAccount(Context context) {
        Account newAccount = new Account(
                context.getString(R.string.sync_account),
                context.getString(R.string.sync_account_type));

        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        accountManager.addAccountExplicitly(newAccount, null, null);

        return newAccount;
    }
}
