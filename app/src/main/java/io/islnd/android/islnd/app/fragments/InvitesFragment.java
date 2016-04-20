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
import io.islnd.android.islnd.app.adapters.InviteAdapter;
import io.islnd.android.islnd.app.adapters.NotificationAdapter;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.loader.InviteLoader;
import io.islnd.android.islnd.app.loader.LoaderId;
import io.islnd.android.islnd.app.loader.NotificationLoader;

public class InvitesFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private Context mContext;
    private LinearLayoutManager mLayoutManager;
    private InviteAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_invites, container, false);

        mContext = getContext();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.invite_recycler_view);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new InviteAdapter(mContext, null);
        final InviteLoader notificationLoader = new InviteLoader(
                mContext,
                IslndContract.InviteEntry.CONTENT_URI,
                mAdapter);
        getLoaderManager().initLoader(
                LoaderId.INVITE_LOADER_ID,
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
                .setTitle(R.string.invites);
    }
}
