package io.islnd.android.islnd.app.fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.activities.NewPostActivity;
import io.islnd.android.islnd.app.adapters.PostAdapter;
import io.islnd.android.islnd.app.PostCollection;
import io.islnd.android.islnd.app.loader.LocalPostLoader;
import io.islnd.android.islnd.app.SimpleDividerItemDecoration;

import io.islnd.android.islnd.messaging.MessageLayer;

public class FeedFragment extends Fragment {
    private final static String TAG = FeedFragment.class.getSimpleName();

    private static final int NEW_POST_RESULT = 1;
    public static final int DELETE_POST_RESULT = 2;

    private RecyclerView mRecyclerView;
    private PostAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;
    private ContentResolver mResolver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.content_feed, container, false);

        // Feed posts setup
        mRecyclerView = (RecyclerView) v.findViewById(R.id.feed_recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PostAdapter(getContext(), null);
        getLoaderManager().initLoader(0, null, new LocalPostLoader(getContext(), mAdapter));

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        // Swipe to refresh
        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_to_refresh_layout);

        mRefreshLayout.setOnRefreshListener(
                () -> {
                    //--TODO get new content from network
                    new GetPostsFromServerTask().execute();
                    mRefreshLayout.setRefreshing(false);
                });

        // TODO: handle onclicks more betterer
        // Fab
        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
            startNewPostActivity();
        });

        return v;
    }

    public void startNewPostActivity() {
        Intent newPostIntent = new Intent(getContext(), NewPostActivity.class);
        startActivityForResult(newPostIntent, NEW_POST_RESULT);
    }

    private class GetPostsFromServerTask extends AsyncTask<Void, Void, PostCollection> {
        private final String TAG = GetPostsFromServerTask.class.getSimpleName();

        @Override
        protected PostCollection doInBackground(Void... params) {
            return MessageLayer.getPosts(getContext());
        }
    }
}
