package io.islnd.android.islnd.app.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import io.islnd.android.islnd.app.adapters.ViewFriendsAdapter;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.User;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.SimpleDividerItemDecoration;

import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.MessageLayer;

import java.util.ArrayList;
import java.util.List;

public class ViewFriendsFragment extends Fragment implements SearchView.OnQueryTextListener {
    private RecyclerView mRecyclerView;
    private ViewFriendsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<User> friendsList;
    private List<User> adapterList;
    private SwipeRefreshLayout refreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.content_view_friends, container, false);
        setHasOptionsMenu(true);

        // Feed posts setup
        friendsList = new ArrayList<>();
        adapterList = new ArrayList<>();
        mRecyclerView = (RecyclerView) v.findViewById(R.id.view_friends_recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ViewFriendsAdapter(adapterList, getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        new GetFriendsTask().execute();

        // Swipe to refresh
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_to_refresh_layout);

        refreshLayout.setOnRefreshListener(() -> {
            // TODO: This will addPost duplicates, okay for now
            new GetFriendsTask().execute();
        });
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.view_friends_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(this);
    }

    private List<User> filter(List<User> friends, String query) {
        query = query.toLowerCase();

        final List<User> filteredList = new ArrayList<>();
        for (User friend : friends) {
            final String text = friend.getUserName().toLowerCase();
            if (text.contains(query)) {
                filteredList.add(friend);
            }
        }
        return filteredList;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<User> filteredModelList = filter(friendsList, newText);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    private class GetFriendsTask extends AsyncTask<Void, Void, Void> {

        private final String TAG = GetFriendsTask.class.getSimpleName();

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        String username = preferences.getString(getContext().getString(R.string.user_name), "");
        String privateKey = preferences.getString(getContext().getString(R.string.private_key), "");

        @Override
        protected Void doInBackground(Void... params) {
            MessageLayer.getReaders(getContext(), username, CryptoUtil.decodePrivateKey(
                            privateKey));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String[] projection = new String[] {
                    IslndContract.UserEntry.COLUMN_USERNAME
            };

            Cursor cursor = getContext().getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            if (!cursor.moveToFirst()) {
                return;
            }

            List<User> allFriends = new ArrayList<>();

            do {
                String username = cursor.getString(cursor.getColumnIndex(IslndContract.UserEntry.COLUMN_USERNAME));
                allFriends.add(new User(username));
            } while (cursor.moveToNext());

            if (allFriends.size() > 0) {
                adapterList.addAll(allFriends);
                mAdapter.notifyDataSetChanged();
            }

            refreshLayout.setRefreshing(false);
            cursor.close();
        }
    }
}
