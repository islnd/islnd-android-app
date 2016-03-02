package com.island.island.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.island.island.Adapters.ViewPostAdapter;
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
import com.island.island.Utils.Utils;

import org.island.messaging.CommentCollection;
import org.island.messaging.MessageLayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ViewPostActivity extends AppCompatActivity
        implements DeletePostFragment.NoticeDeletePostListener,
        DeleteCommentFragment.NoticeDeleteCommentListener
{
    private Post mPost = null;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList mViewPostList;
    private Set<CommentKey> mCommentMap;

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
        mAdapter = new ViewPostAdapter(mViewPostList, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

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

        // Get comments from network
        new GetCommentsTask().execute(mPost);

        // Swipe to refresh
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);
        refreshLayout.setOnRefreshListener(() ->
        {
            // TODO: Run async task again
            new GetCommentsTask().execute(mPost);
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
            Comment comment = IslandDB.addCommentToPost(
                    this,
                    mPost,
                    commentText);

            addCommentToPostAndTimeline(comment);

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
        removeCommentFromPostAndTimeline(commentKey);

        mAdapter.notifyDataSetChanged();
    }

    private class GetCommentsTask extends AsyncTask<Post, Void, CommentCollection> {
        private final String TAG = GetCommentsTask.class.getSimpleName();

        protected CommentCollection doInBackground(Post... params) {
            Log.v(TAG, "starting get comments task");
            return MessageLayer.getCommentCollection(
                    getApplicationContext(),
                    mPost.getUserId(),
                    mPost.getPostId());
        }

        @Override
        protected void onPostExecute(CommentCollection commentCollection) {
            boolean commentsUpdated = false;

            //-add comments
            List<CommentViewModel> comments = Utils.buildCommentViewModels(
                    getApplicationContext(),
                    commentCollection.getCommentsGroupedByPostKey()
                            .get(mPost.getKey()));
            for (CommentViewModel comment : comments) {
                if (addCommentToPostAndTimeline(comment)) {
                    commentsUpdated = true;
                }
            }

            //--delete comments
            List<CommentKey> deletions = commentCollection
                    .getDeletions()
                    .get(mPost.getKey());
            for (CommentKey deletion : deletions) {
                if (removeCommentFromPostAndTimeline(deletion)) {
                    commentsUpdated = true;
                }
            }

            if (commentsUpdated) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private boolean removeCommentFromPostAndTimeline(CommentKey keyToDelete) {
        if (!mCommentMap.contains(keyToDelete)) {
            return false;
        }

        mPost.deleteComment(keyToDelete);
        mCommentMap.remove(keyToDelete);
        int index = findComment(keyToDelete);
        mViewPostList.remove(index);
        return true;
    }

    private boolean addCommentToPostAndTimeline(Comment comment) {
        return addCommentToPostAndTimeline(Utils.buildCommentViewModel(this, comment));
    }

    private boolean addCommentToPostAndTimeline(CommentViewModel commentViewModel) {
        if (mCommentMap.contains(commentViewModel.getKey())) {
            return false;
        }

        mCommentMap.add(commentViewModel.getKey());
        mPost.addComment(commentViewModel);
        mViewPostList.add(commentViewModel);
        return true;
    }

    private int findComment(CommentKey commentKey) {
        for (int i = 1; i < mViewPostList.size(); i++) {
            if (((CommentViewModel)mViewPostList.get(i)).getKey().equals(commentKey)) {
                return i;
            }
        }

        return -1;
    }
}
