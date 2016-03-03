package com.island.island.Activities;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.island.island.Adapters.PostAdapter;
import com.island.island.Database.CommentDatabase;
import com.island.island.Database.IslndContract;
import com.island.island.DeletePostFragment;
import com.island.island.Models.CommentKey;
import com.island.island.Models.CommentViewModel;
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
import java.util.List;
import java.util.Map;

public class FeedActivity extends NavBaseActivity implements
        DeletePostFragment.NoticeDeletePostListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = FeedActivity.class.getSimpleName();

    private static final int NEW_POST_RESULT = 1;
    public static final int DELETE_POST_RESULT = 2;

    private RecyclerView mRecyclerView;
    private PostAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;
    private boolean mAdapterInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Feed posts setup
        mRecyclerView = (RecyclerView) findViewById(R.id.feed_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        getLoaderManager().initLoader(0, null, this);

        // Swipe to refresh
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);

        mRefreshLayout.setOnRefreshListener(
                () -> {
                    //--TODO get new content from network
                    new GetPostsFromServerTask().execute();
                    mRefreshLayout.setRefreshing(false);
                });
    }

    private List<CommentViewModel> getCommentsForPost(CommentDatabase commentDatabase, RawPost post) {
        List<Comment> comments = commentDatabase.getComments(post.getUserId(), post.getPostId());
        return Utils.buildCommentViewModels(this, comments);
    }

    public void startNewPostActivity(View view) {
        Intent newPostIntent = new Intent(FeedActivity.this, NewPostActivity.class);
        startActivityForResult(newPostIntent, NEW_POST_RESULT);
    }

    @Override
    public void onDeletePostDialogPositiveClick(DialogFragment dialogFragment) {
        //--TODO fix deletes
//        Bundle args = dialogFragment.getArguments();
//        String postId = args.getString(DeletePostFragment.POST_ID_BUNDLE_KEY);
//        int postUserId = args.getInt(DeletePostFragment.USER_ID_BUNDLE_KEY);
//        final PostKey postKey = new PostKey(postUserId, postId);
//        removePostFromFeed(postKey);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(TAG, "onCreateLoader");
        if (id != 0) return null;

        String[] projection = new String[]{
                IslndContract.UserEntry.COLUMN_USERNAME,
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry._ID,
                IslndContract.PostEntry.COLUMN_USER_ID,
                IslndContract.PostEntry.COLUMN_POST_ID,
                IslndContract.PostEntry.COLUMN_TIMESTAMP,
                IslndContract.PostEntry.COLUMN_CONTENT,
        };

        return new CursorLoader(
                this,
                IslndContract.PostEntry.CONTENT_URI,
                projection,
                null,
                null,
                IslndContract.PostEntry.COLUMN_TIMESTAMP + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(TAG, "onLoadFinished");
        if (mAdapterInitialized) {
            mAdapter.swapCursor(data);
        }
        else {
            mAdapterInitialized = true;
            mAdapter = new PostAdapter(this, data);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(TAG, "onLoaderReset");
        mAdapter.swapCursor(null);
    }

    private class GetCommentsFromServerTask extends AsyncTask<Void, Void, CommentCollection> {
        private final String TAG = GetCommentsFromServerTask.class.getSimpleName();

        @Override
        protected CommentCollection doInBackground(Void... params) {
            List<CommentQuery> commentQueries = new ArrayList<>();
//            FriendDatabase friendDatabase = FriendDatabase.getInstance(getApplicationContext());

//            for (Post post : mArrayOfPosts) {
//                String postAuthorPseudonym = friendDatabase.getPseudonym(post.getUserId());
//                commentQueries.add(new CommentQuery(postAuthorPseudonym, post.getPostId()));
//            }

            return MessageLayer.getCommentCollection(getApplicationContext(), commentQueries);
        }

        @Override
        protected void onPostExecute(CommentCollection commentCollection) {
            boolean anyPostUpdated = false;

            Map<PostKey, List<Comment>> postKeyToComments = commentCollection.getCommentsGroupedByPostKey();
            for (PostKey postKey : postKeyToComments.keySet()) {

                List<CommentViewModel> commentViewModels = Utils.buildCommentViewModels(
                        getApplicationContext(),
                        postKeyToComments.get(postKey));
            }

            Map<PostKey, List<CommentKey>> postKeyToDeletions = commentCollection.getDeletions();
            for (PostKey postKey : postKeyToDeletions.keySet()) {
                List<CommentKey> deletions = postKeyToDeletions.get(postKey);
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
    }
}
