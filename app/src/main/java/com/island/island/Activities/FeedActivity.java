package com.island.island.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.island.island.Adapters.PostAdapter;
import com.island.island.Database.FriendDatabase;
import com.island.island.Database.PostDatabase;
import com.island.island.Models.Post;
import com.island.island.Models.RawPost;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;
import com.island.island.Utils.Utils;

import org.island.messaging.MessageLayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeedActivity extends NavBaseActivity {
    private final static String TAG = FeedActivity.class.getSimpleName();

    private final static int NEW_POST_RESULT = 1;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Post> mArrayOfPosts;
    private Set<String> mPostMap;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Feed posts setup
        mArrayOfPosts = new ArrayList<>();
        mPostMap = new HashSet<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.feed_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PostAdapter(this, mArrayOfPosts);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        // Populate feed
        getPostsFromDatabase();
        new GetPostsFromServerTask().execute();

        // Swipe to refresh
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);

        mRefreshLayout.setOnRefreshListener(() -> {
            new GetPostsFromServerTask().execute();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_POST_RESULT) {
            if (data != null) {
                Post post = (Post) data.getSerializableExtra(Post.POST_EXTRA);
                mArrayOfPosts.add(0, post);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void getPostsFromDatabase() {
        List<RawPost> localPosts = PostDatabase.getInstance(this).getAll();
        FriendDatabase friendDatabase = FriendDatabase.getInstance(this);
        boolean listChanged = false;
        for (RawPost p : localPosts) {
            if (addPostToFeed(friendDatabase, p)) {
                listChanged = true;
            }
        }

        if (listChanged) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private boolean addPostToFeed(FriendDatabase friendDatabase, RawPost p) {
        int userId = p.getUserId();
        String postAuthor = userId == 0
                ? Utils.getUser(this)
                : friendDatabase.getUsername(userId);

        String key = postAuthor + p.getTimestamp();
        if (mPostMap.contains(key)) {
            return false;
        }

        mPostMap.add(key);
        int insertionPoint = getIndexToInsertPost(p.getTimestamp());
        mArrayOfPosts.add(
                insertionPoint,
                new Post(
                        postAuthor,
                        p.getTimestamp(),
                        p.getContent(),
                        new ArrayList<>()
                ));

        return true;
    }

    public void startNewPostActivity(View view) {
        Intent newPostIntent = new Intent(FeedActivity.this, NewPostActivity.class);
        startActivityForResult(newPostIntent, NEW_POST_RESULT);
    }

    private class GetPostsFromServerTask extends AsyncTask<Void, Void, List<Post>> {
        private final String TAG = GetPostsFromServerTask.class.getSimpleName();

        @Override
        protected List<Post> doInBackground(Void... params) {
            return MessageLayer.getPosts(getApplicationContext());
        }

        @Override
        protected void onPostExecute(List<Post> posts) {
            if (posts != null) {
                boolean adapterChanged = addPostsToFeed(posts);
                if (adapterChanged) {
                    mAdapter.notifyDataSetChanged();
                }
            }

            mRefreshLayout.setRefreshing(false);
            Utils.printAvailableMemory(getApplicationContext(), TAG);
        }

        private boolean addPostsToFeed(List<Post> posts) {
            boolean postAdded = false;
            for (Post p : posts) {
                if (!mPostMap.contains(p.getKey())) {
                    mPostMap.add(p.getKey());

                    int insertionPoint = getIndexToInsertPost(p.getTimestamp());
                    mArrayOfPosts.add(insertionPoint, p);
                    postAdded = true;
                }
            }

            return postAdded;
        }
    }

    private int getIndexToInsertPost(long postTimestamp) {
        int insertionPoint = 0;
        while (insertionPoint < mArrayOfPosts.size()
                && postTimestamp < mArrayOfPosts.get(insertionPoint).getTimestamp()) {
            Log.v(TAG, "" + insertionPoint);
            insertionPoint++;
        }

        return insertionPoint;
    }
}
