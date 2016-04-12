package io.islnd.android.islnd.app.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.islnd.android.islnd.app.IslndAction;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.SimpleDividerItemDecoration;
import io.islnd.android.islnd.app.StopRefreshReceiver;
import io.islnd.android.islnd.app.adapters.NotificationAdapter;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.loader.LoaderId;
import io.islnd.android.islnd.app.loader.NotificationLoader;
import io.islnd.android.islnd.app.util.Util;

public class NotificationsFragment extends Fragment {

    private Context mContext;
    private ContentResolver mResolver;
    private RecyclerView mRecyclerView;
    private NotificationAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;
    private StopRefreshReceiver mStopRefreshReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_notifications, container, false);

        mContext = getContext();
        mResolver = mContext.getContentResolver();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.notification_recycler_view);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NotificationAdapter(mContext, null);
        final NotificationLoader notificationLoader = new NotificationLoader(
                mContext,
                IslndContract.NotificationEntry.CONTENT_URI,
                mAdapter);
        getLoaderManager().initLoader(
                LoaderId.NOTIFICATION_LOADER_ID,
                null,
                notificationLoader);

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

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(R.string.notifications);
        IntentFilter filter = new IntentFilter(IslndAction.EVENT_SYNC_COMPLETE);
        getContext().registerReceiver(mStopRefreshReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(mStopRefreshReceiver);
    }
}
