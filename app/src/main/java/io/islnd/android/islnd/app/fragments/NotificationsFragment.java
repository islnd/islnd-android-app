package io.islnd.android.islnd.app.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.SimpleDividerItemDecoration;
import io.islnd.android.islnd.app.adapters.NotificationAdapter;
import io.islnd.android.islnd.app.adapters.PostAdapter;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.loader.LoaderId;
import io.islnd.android.islnd.app.loader.NotificationLoader;
import io.islnd.android.islnd.app.loader.PostLoader;

public class NotificationsFragment extends Fragment {

    private Context mContext;
    private ContentResolver mResolver;
    private RecyclerView mRecyclerView;
    private NotificationAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_notifications, container, false);

        mContext = getContext();
        mResolver = mContext.getContentResolver();

        // Feed posts setup
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

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(R.string.notifications);
    }
}
