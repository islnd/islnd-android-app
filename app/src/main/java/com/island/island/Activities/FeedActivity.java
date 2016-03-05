package com.island.island.Activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.island.island.Adapters.PostAdapter;
import com.island.island.Database.IslndContract;
import com.island.island.DeletePostFragment;
import com.island.island.PostCollection;
import com.island.island.PostLoader;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;

import org.island.messaging.MessageLayer;

public class FeedActivity extends NavBaseActivity implements
        DeletePostFragment.NoticeDeletePostListener {

    private final static String TAG = FeedActivity.class.getSimpleName();

    private static final int NEW_POST_RESULT = 1;
    public static final int DELETE_POST_RESULT = 2;

    private RecyclerView mRecyclerView;
    private PostAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;
    private ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Feed posts setup
        mRecyclerView = (RecyclerView) findViewById(R.id.feed_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PostAdapter(this, null);
        getLoaderManager().initLoader(0, null, new PostLoader(this, mAdapter));

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

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
