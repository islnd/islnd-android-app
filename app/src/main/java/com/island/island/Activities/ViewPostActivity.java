package com.island.island.Activities;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.island.island.Adapters.CursorRecyclerViewAdapter;
import com.island.island.Adapters.ViewPostAdapter;
import com.island.island.Database.IslndContract;
import com.island.island.DeleteCommentFragment;
import com.island.island.DeletePostFragment;
import com.island.island.Models.Comment;
import com.island.island.Models.CommentKey;
import com.island.island.Models.CommentViewModel;
import com.island.island.Models.Post;
import com.island.island.Database.IslandDB;
import com.island.island.Models.PostKey;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;

import org.island.messaging.CommentCollection;
import org.island.messaging.MessageLayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ViewPostActivity extends AppCompatActivity
        implements DeletePostFragment.NoticeDeletePostListener,
        DeleteCommentFragment.NoticeDeleteCommentListener,
        LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int COMMENT_LOADER_ID = 0;
    private static final String POST_AUTHOR_ID_BUNDLE_KEY = "post_author_bundle_key";
    private static final String POST_ID_BUNDLE_KEY = "post_id_bundle_key";

    private Post mPost = null;

    private RecyclerView mRecyclerView;
    private CursorRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList mViewPostList;
    private Set<CommentKey> mCommentMap;
    private boolean mAdapterInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Boilerplate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // List view stuff
        mViewPostList = new ArrayList<>();
        mCommentMap = new HashSet<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.view_post_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Get intent with post info
        Intent intent = getIntent();
        mPost = (Post)intent.getSerializableExtra(Post.POST_EXTRA);
        mViewPostList.add(mPost);

        // Add comments to list
        List<CommentViewModel> comments = mPost.getComments();
        mViewPostList.addAll(comments);
        for (CommentViewModel comment : comments) {
            mCommentMap.add(comment.getKey());
        }

        Bundle args = new Bundle();
        args.putInt(POST_AUTHOR_ID_BUNDLE_KEY, mPost.getUserId());
        args.putString(POST_ID_BUNDLE_KEY, mPost.getPostId());
        getLoaderManager().initLoader(COMMENT_LOADER_ID, args, this);

        // Swipe to refresh
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);
        refreshLayout.setOnRefreshListener(() ->
        {
            // TODO: Run async task again
            AsyncTaskLoader getComments = new GetCommentsLoader(this, mPost.getUserId(), mPost.getPostId());
            getComments.forceLoad();
            refreshLayout.setRefreshing(false);
        });
    }

    public void addCommentToPost(View view)
    {
        EditText addCommentEditText = (EditText) findViewById(R.id.post_comment_edit_text);
        String commentText = addCommentEditText.getText().toString();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        if(commentText.equals(""))
        {
            Snackbar.make(view, getString(R.string.empty_comment_post), Snackbar.LENGTH_SHORT).show();
        }
        else
        {
            IslandDB.addCommentToPost(
                    this,
                    mPost,
                    commentText);

            //--Clear edit text
            addCommentEditText.setText("");
            imm.hideSoftInputFromWindow(addCommentEditText.getWindowToken(), 0);
        }
    }

    @Override
    public void onDeletePostDialogPositiveClick(DialogFragment dialogFragment) {
        setResult(Activity.RESULT_OK, buildReturnIntent(mPost.getUserId(), mPost.getPostId()));
        finish();
    }

    @NonNull
    private Intent buildReturnIntent(int postAuthorId, String postId) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(
                PostKey.POST_KEY_EXTRA,
                new PostKey(postAuthorId, postId));
        return returnIntent;
    }

    @Override
    public void onDeleteCommentDialogPositiveClick(DialogFragment dialogFragment) {
        Bundle args = dialogFragment.getArguments();
        int commentUserId = args.getInt(DeleteCommentFragment.COMMENT_USER_ID_BUNDLE_KEY);
        String commentId = args.getString(DeleteCommentFragment.COMMENT_ID_BUNDLE_KEY);

        final CommentKey commentKey = new CommentKey(commentUserId, commentId);
        //--TODO do the delete!

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == COMMENT_LOADER_ID) {
            String[] projection = new String[]{
                    IslndContract.UserEntry.COLUMN_USERNAME,
                    IslndContract.CommentEntry.TABLE_NAME + "." + IslndContract.CommentEntry._ID,
                    IslndContract.CommentEntry.COLUMN_POST_USER_ID,
                    IslndContract.CommentEntry.COLUMN_POST_ID,
                    IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID,
                    IslndContract.CommentEntry.COLUMN_COMMENT_ID,
                    IslndContract.CommentEntry.COLUMN_TIMESTAMP,
                    IslndContract.CommentEntry.COLUMN_CONTENT,
            };

            int postAuthorId = args.getInt(POST_AUTHOR_ID_BUNDLE_KEY);
            String postId = args.getString(POST_ID_BUNDLE_KEY);
            return new CursorLoader(
                    this,
                    IslndContract.CommentEntry.buildCommentUriWithPostAuthorIdAndPostId(postAuthorId, postId),
                    projection,
                    null,
                    null,
                    IslndContract.CommentEntry.COLUMN_TIMESTAMP + " DESC"
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapterInitialized) {
            mAdapter.swapCursor(data);
        } else {
            mAdapterInitialized = true;
            mAdapter = new ViewPostAdapter(this, data);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //--TODO ?
    }

    private class GetCommentsLoader extends AsyncTaskLoader<Void> {

        private final int postUserId;
        private final String postId;
        private final ContentResolver mContentResolver;

        public GetCommentsLoader(Context context, int postUserId, String postId) {
            super(context);
            this.postUserId = postUserId;
            this.postId = postId;
            mContentResolver = context.getContentResolver();
        }

        @Override
        public Void loadInBackground() {
            CommentCollection commentCollection = MessageLayer.getCommentCollection(
                    getApplicationContext(),
                    postUserId,
                    postId);

            ContentValues[] values =
                    convertCommentsToContentValues(
                            commentCollection.getCommentsGroupedByPostKey().get(new PostKey(postUserId, postId)),
                            postUserId,
                            postId);

            mContentResolver.bulkInsert(
                    IslndContract.CommentEntry.CONTENT_URI,
                    values);

            return null;
        }
    }

    private ContentValues[] convertCommentsToContentValues(List<Comment> commentUpdates, int postUserId, String postId) {
        ContentValues[] values = new ContentValues[commentUpdates.size()];
        for (int i = 0; i < commentUpdates.size(); i++) {
            Comment commentUpdate = commentUpdates.get(i);

            values[i] = new ContentValues();
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_POST_USER_ID,
                    postUserId);
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_POST_ID,
                    postId);
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID,
                    commentUpdate.getCommentUserId());
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_COMMENT_ID,
                    commentUpdate.getCommentId());
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_TIMESTAMP,
                    commentUpdate.getTimestamp());
            values[i].put(
                    IslndContract.CommentEntry.COLUMN_CONTENT,
                    commentUpdate.getContent());
        }

        return values;
    }
}
