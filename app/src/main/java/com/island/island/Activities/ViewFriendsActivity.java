package com.island.island.Activities;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.island.island.Adapters.ViewFriendsAdapter;
import com.island.island.Models.User;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;

import org.island.messaging.Crypto;
import org.island.messaging.MessageLayer;

import java.util.ArrayList;
import java.util.List;

public class ViewFriendsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener
{
    private RecyclerView mRecyclerView;
    private ViewFriendsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<User> friendsList;
    private List<User> adapterList;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Feed posts setup
        friendsList = new ArrayList<>();
        adapterList = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.view_friends_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ViewFriendsAdapter(adapterList, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        new GetFriendsTask().execute();

        // Swipe to refresh
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);

        refreshLayout.setOnRefreshListener(() ->
        {
            // TODO: Run async task again
            refreshLayout.setRefreshing(false);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_friends_menu, menu);

        /*
        https://stackoverflow.com/questions/30398247/how-to-filter-a-recyclerview-with-a-searchview
         */
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    private List<User> filter(List<User> friends, String query)
    {
        query = query.toLowerCase();

        final List<User> filteredList = new ArrayList<>();
        for (User friend : friends)
        {
            final String text = friend.getUserName().toLowerCase();
            if (text.contains(query))
            {
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
    public boolean onQueryTextChange(String newText)
    {
        final List<User> filteredModelList = filter(friendsList, newText);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    private class GetFriendsTask extends AsyncTask<Void, Void, List<User>> {

        private final String TAG = GetFriendsTask.class.getSimpleName();

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = preferences.getString(getApplicationContext().getString(R.string.user_name), "");
        String privateKey = preferences.getString(getApplicationContext().getString(R.string.private_key), "");

        @Override
        protected List<User> doInBackground(Void... params) {
            Log.v(TAG, "starting to get friends");
            Log.v(TAG, "username " + username);
            Log.v(TAG, "private key " + privateKey);
            return MessageLayer.getReaders(getApplicationContext(), username, Crypto.decodePrivateKey(privateKey));
        }

        @Override
        protected void onPostExecute(List<User> users) {
            if (users != null) {
                adapterList.addAll(users);
                mAdapter.notifyDataSetChanged();
                for (User u : users) {
                    Log.v(TAG, "added user " + u.getUserName());
                }
            }
        }
    }
}
