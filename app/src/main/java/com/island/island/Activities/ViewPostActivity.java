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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.island.island.Adapters.ViewPostAdapter;
import com.island.island.DeleteCommentFragment;
import com.island.island.DeletePostFragment;
import com.island.island.Models.CommentKey;
import com.island.island.Models.CommentViewModel;
import com.island.island.Models.Post;
import com.island.island.Database.IslandDB;
import com.island.island.Models.PostKey;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;

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
        //--TODO remove the comment from this activity
    }

    private class GetCommentsTask extends AsyncTask<Post, Void, Void> {
        @Override
        protected Void doInBackground(Post... params) {
            List<CommentViewModel> comments = MessageLayer.getCommentCollection(
                    getApplicationContext(),
                    mPost.getUserId(),
                    mPost.getPostId());

            for (CommentViewModel comment : comments) {
                if (!mCommentMap.contains(comment.getKey())) {
                    mCommentMap.add(comment.getKey());
                    mViewPostList.add(comment);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
