package io.islnd.android.islnd.app.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.islnd.android.islnd.app.IslndAction;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.SimpleDividerItemDecoration;
import io.islnd.android.islnd.app.StopRefreshReceiver;
import io.islnd.android.islnd.app.activities.NavBaseActivity;
import io.islnd.android.islnd.app.activities.NewPostActivity;
import io.islnd.android.islnd.app.adapters.PostAdapter;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.loader.LoaderId;
import io.islnd.android.islnd.app.loader.PostLoader;
import io.islnd.android.islnd.app.util.Util;

public class FeedFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

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
    private TextView mNotificationBadgeCount;
    private int mNotificationCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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
                LoaderId.POST_LOADER_ID,
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

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
            startNewPostActivity();
        });

        getActivity().getSupportLoaderManager().initLoader(LoaderId.FEED_NOTIFICATION_COUNT_LOADER_ID, new Bundle(), this);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.feed_menu, menu);

        MenuItem notificationBadge = menu.findItem(R.id.notification_badge);
        View actionView = MenuItemCompat.getActionView(notificationBadge);

        actionView.setOnClickListener((View view) -> {
            ((NavBaseActivity) getActivity()).notificationBadgeClick();
        });

        mNotificationBadgeCount =
                (TextView) actionView.findViewById(R.id.notification_badge_count);
        setNotificationBadgeCount();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(R.string.app_name);
        IntentFilter filter = new IntentFilter(IslndAction.EVENT_SYNC_COMPLETE);
        getContext().registerReceiver(mStopRefreshReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(mStopRefreshReceiver);
    }

    private void startNewPostActivity() {
        Intent newPostIntent = new Intent(mContext, NewPostActivity.class);
        startActivityForResult(newPostIntent, NEW_POST_RESULT);
    }

    private void setNotificationBadgeCount() {
        if (mNotificationCount > 0 && mNotificationCount < 100) {
            mNotificationBadgeCount.setVisibility(View.VISIBLE);
            mNotificationBadgeCount.setText(Integer.toString(mNotificationCount));
        } else if (mNotificationCount >= 100) {
            mNotificationBadgeCount.setVisibility(View.VISIBLE);
            mNotificationBadgeCount.setText(getString(R.string.notification_badge_99_plus));
        } else {
            mNotificationBadgeCount.setVisibility(View.INVISIBLE);
            mNotificationBadgeCount.setText("");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(TAG, "create loader " + id);
        String[] projection = new String[]{
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry._ID
        };

        String selection =
                IslndContract.NotificationEntry.TABLE_NAME
                        + "."
                        + IslndContract.NotificationEntry.COLUMN_ACTIVE
                        + " = ?";
        String[] selectionArgs = new String[]{
                Integer.toString(IslndContract.NotificationEntry.ACTIVE)
        };

        return new CursorLoader(
                mContext,
                IslndContract.NotificationEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mNotificationCount = data.getCount();

        if (mNotificationBadgeCount != null) {
            setNotificationBadgeCount();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
