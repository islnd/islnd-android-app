package io.islnd.android.islnd.app.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.islnd.android.islnd.app.IslndIntent;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.SimpleDividerItemDecoration;
import io.islnd.android.islnd.app.StopRefreshReceiver;
import io.islnd.android.islnd.app.activities.NewPostActivity;
import io.islnd.android.islnd.app.adapters.PostAdapter;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.loader.PostLoader;
import io.islnd.android.islnd.app.util.Util;

public class FeedFragment extends Fragment {
    private final static String TAG = FeedFragment.class.getSimpleName();

    private static final int NEW_POST_RESULT = 1;
    public static final int DELETE_POST_RESULT = 2;

    private Context mContext;
    private ContentResolver mResolver;
    private RecyclerView mRecyclerView;
    private PostAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;
    private StopRefreshReceiver mStopRefreshReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.content_feed, container, false);
        mContext = getContext();
        mResolver = mContext.getContentResolver();

        // Feed posts setup
        mRecyclerView = (RecyclerView) v.findViewById(R.id.feed_recycler_view);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PostAdapter(mContext, null);
        final PostLoader postLoader = new PostLoader(
                mContext,
                IslndContract.PostEntry.CONTENT_URI,
                mAdapter);
        getLoaderManager().initLoader(
                PostLoader.LOADER_ID,
                null,
                postLoader);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));

        // Swipe to refresh
        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_to_refresh_layout);
        mRefreshLayout.setOnRefreshListener(
                () -> {
                    mResolver.requestSync(
                            Util.getSyncAccount(mContext),
                            IslndContract.CONTENT_AUTHORITY,
                            new Bundle());
                });
        mStopRefreshReceiver = new StopRefreshReceiver(mRefreshLayout);

        // TODO: handle onclicks more betterer
        // Fab
        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
            startNewPostActivity();
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(R.string.app_name);
        IntentFilter filter = new IntentFilter(IslndIntent.EVENT_SYNC_COMPLETE);
        getContext().registerReceiver(mStopRefreshReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(mStopRefreshReceiver);
    }

    public void startNewPostActivity() {
        Intent newPostIntent = new Intent(mContext, NewPostActivity.class);
        startActivityForResult(newPostIntent, NEW_POST_RESULT);
    }
}
