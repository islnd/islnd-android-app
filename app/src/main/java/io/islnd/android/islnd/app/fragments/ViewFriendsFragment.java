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

import io.islnd.android.islnd.app.IslndIntent;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.SimpleDividerItemDecoration;
import io.islnd.android.islnd.app.StopRefreshReceiver;
import io.islnd.android.islnd.app.adapters.ViewFriendsAdapter;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.loader.FriendLoader;
import io.islnd.android.islnd.app.util.Util;

public class ViewFriendsFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private ViewFriendsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;
    private StopRefreshReceiver mStopRefreshReceiver;

    private Context mContext;
    private ContentResolver mResolver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_view_friends, container, false);
        setHasOptionsMenu(true);

        mContext = getContext();
        mResolver = mContext.getContentResolver();

        // Feed posts setup
        mRecyclerView = (RecyclerView) v.findViewById(R.id.view_friends_recycler_view);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ViewFriendsAdapter(mContext, null);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));

        //--Load the friends
        Bundle args = new Bundle();
        args.putInt(FriendLoader.USER_ID_BUNDLE_KEY, Util.getUserId(mContext));
        FriendLoader friendLoader = new FriendLoader(
                mContext,
                mAdapter);
        getLoaderManager().initLoader(FriendLoader.LOADER_ID, args, friendLoader);

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
                .setTitle(R.string.view_friends_fragment);
        IntentFilter filter = new IntentFilter(IslndIntent.EVENT_SYNC_COMPLETE);
        getContext().registerReceiver(mStopRefreshReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(mStopRefreshReceiver);
    }
}
