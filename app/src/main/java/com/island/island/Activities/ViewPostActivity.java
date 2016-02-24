package com.island.island.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.island.island.Adapters.ViewPostAdapter;
import com.island.island.Database.FriendDatabase;
import com.island.island.Models.Comment;
import com.island.island.Models.Post;
import com.island.island.Database.IslandDB;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;
import com.island.island.Utils.Utils;

import org.island.messaging.MessageLayer;
import org.island.messaging.PseudonymKey;
import org.island.messaging.Util;

import java.util.ArrayList;
import java.util.List;


public class ViewPostActivity extends AppCompatActivity
{
    private Post post = null;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList mViewPostList;

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
        mRecyclerView = (RecyclerView) findViewById(R.id.view_post_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ViewPostAdapter(mViewPostList, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        // Get intent with post info
        Intent intent = getIntent();
        post = (Post)intent.getSerializableExtra(Post.POST_EXTRA);
        mViewPostList.add(post);

        // Add comments to list
        List<Comment> comments = post.getComments();
        mViewPostList.addAll(comments);

        // Get comments from network
        FriendDatabase friendDatabase = FriendDatabase.getInstance(this);
        new GetCommentsTask().execute(post.getUserName(), post.getPostId());

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
            IslandDB.addCommentToPost(this, post, new Comment(Utils.getUser(this), commentText));
            addCommentEditText.setText("");
            imm.hideSoftInputFromWindow(addCommentEditText.getWindowToken(), 0);
        }
    }

    private class GetCommentsTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            PseudonymKey pk = FriendDatabase.getInstance(getApplicationContext()).getKey(params[0]);
            String postId = params[1];
            List<Comment> comments = MessageLayer.getComments(getApplicationContext(), pk, postId);
            mViewPostList.addAll(comments);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
