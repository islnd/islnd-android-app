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
import com.island.island.Database.CommentDatabase;
import com.island.island.Database.FriendDatabase;
import com.island.island.Database.PostDatabase;
import com.island.island.Models.CommentViewModel;
import com.island.island.Models.Post;
import com.island.island.Models.Comment;
import com.island.island.Models.PostKey;
import com.island.island.Models.RawPost;
import com.island.island.PostCollection;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;
import com.island.island.Utils.Utils;

import org.island.messaging.MessageLayer;
import org.island.messaging.CommentCollection;
import org.island.messaging.server.CommentQuery;

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
    private Set<PostKey> mPostMap;
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
                mPostMap.add(post.getKey());
            }
        }
    }

    private void getPostsFromDatabase() {
        List<RawPost> localPosts = PostDatabase.getInstance(this).getAll();
        FriendDatabase friendDatabase = FriendDatabase.getInstance(this);
        CommentDatabase commentDatabase = CommentDatabase.getInstance(this);
        boolean listChanged = false;
        for (RawPost p : localPosts) {
            if (addPostToFeed(friendDatabase, commentDatabase, p)) {
                listChanged = true;
            }
        }

        if (listChanged) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private boolean addPostToFeed(FriendDatabase friendDatabase, CommentDatabase commentDatabase, RawPost p) {
        int userId = p.getUserId();
        String postAuthor = userId == 0
                ? Utils.getUser(this)
                : friendDatabase.getUsername(userId);

        final Post post = new Post(
                postAuthor,
                userId,
                p.getPostId(),
                p.getTimestamp(),
                p.getContent(),
                getCommentsForPost(commentDatabase, p)
        );

        if (mPostMap.contains(post.getKey())) {
            return false;
        }

        mPostMap.add(post.getKey());
        int insertionPoint = getIndexToInsertPost(p.getTimestamp());
        mArrayOfPosts.add(insertionPoint, post);

        return true;
    }

    private List<CommentViewModel> getCommentsForPost(CommentDatabase commentDatabase, RawPost post) {
        List<Comment> comments = commentDatabase.getComments(post.getUserId(), post.getPostId());
        return Utils.buildCommentViewModels(this, comments);
    }

    public void startNewPostActivity(View view) {
        Intent newPostIntent = new Intent(FeedActivity.this, NewPostActivity.class);
        startActivityForResult(newPostIntent, NEW_POST_RESULT);
    }

    private class GetCommentsFromServerTask extends AsyncTask<Void, Void, CommentCollection> {
        private final String TAG = GetCommentsFromServerTask.class.getSimpleName();

        @Override
        protected CommentCollection doInBackground(Void... params) {
            List<CommentQuery> commentQueries = new ArrayList<>();
            FriendDatabase friendDatabase = FriendDatabase.getInstance(getApplicationContext());

            for (Post post : mArrayOfPosts) {
                String postAuthorPseudonym = friendDatabase.getPseudonym(post.getUserId());
                commentQueries.add(new CommentQuery(postAuthorPseudonym, post.getPostId()));
            }

            return MessageLayer.getCommentCollection(getApplicationContext(), commentQueries);
        }

        @Override
        protected void onPostExecute(CommentCollection commentCollection) {
            boolean anyPostUpdated = false;

            for (Post post : mArrayOfPosts) {
                List<Comment> commentsForPost = commentCollection.getComments(
                        post.getUserId(),
                        post.getPostId());
                List<CommentViewModel> commentViewModels = Utils.buildCommentViewModels(
                        getApplicationContext(),
                        commentsForPost);
                if (post.addComments(commentViewModels)) {
                    anyPostUpdated = true;
                }
            }

            if (anyPostUpdated) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class GetPostsFromServerTask extends AsyncTask<Void, Void, PostCollection> {
        private final String TAG = GetPostsFromServerTask.class.getSimpleName();

        @Override
        protected PostCollection doInBackground(Void... params) {
            return MessageLayer.getPosts(getApplicationContext());
        }

        @Override
        protected void onPostExecute(PostCollection postCollection) {
            if (postCollection != null) {
                boolean adapterChanged = addPostsToFeed(postCollection);
                if (adapterChanged) {
                    mAdapter.notifyDataSetChanged();
                }
            }

            mRefreshLayout.setRefreshing(false);
            Utils.printAvailableMemory(getApplicationContext(), TAG);

            new GetCommentsFromServerTask().execute();
        }

        private boolean addPostsToFeed(PostCollection postCollection) {
            boolean postsModified = false;
            for (Post post : postCollection.getPosts()) {
                Log.v(TAG, String.format("%s %s", post.getContent(), post.getKey()));
                if (!mPostMap.contains(post.getKey())) {
                    mPostMap.add(post.getKey());
                    int insertionPoint = getIndexToInsertPost(post.getTimestamp());
                    mArrayOfPosts.add(insertionPoint, post);
                    postsModified = true;
                }
            }

            for (PostKey postKey : postCollection.getDeletedKeys()) {
                int index = findPost(postKey);
                if (index != -1) {
                    mArrayOfPosts.remove(index);
                    postsModified = true;
                    Log.d(TAG, "removing post at index " + index);
                }
                else {
                    Log.d(TAG, "tried to delete post not in feed");
                }
            }

            return postsModified;
        }

        private int findPost(PostKey postKey) {
            for (int i = 0; i < mArrayOfPosts.size(); i++) {
                if (mArrayOfPosts.get(i).getKey().equals(postKey)) {
                    return i;
                }
            }

            return -1;
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
