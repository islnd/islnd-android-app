package com.island.island.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.island.island.Adapters.ViewPostAdapter;
import com.island.island.loader.LocalCommentLoader;
import com.island.island.loader.NetworkCommentLoader;
import com.island.island.Models.Post;
import com.island.island.Database.IslandDB;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;

public class ViewPostActivity extends AppCompatActivity {

    private Post mPost = null;

    private RecyclerView mRecyclerView;
    private ViewPostAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Boilerplate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get intent with post info
        Intent intent = getIntent();
        mPost = (Post)intent.getSerializableExtra(Post.POST_EXTRA);

        // List view stuff
        mRecyclerView = (RecyclerView) findViewById(R.id.view_post_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ViewPostAdapter(this, null, mPost.getKey());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        // Load the local comments
        Bundle args = new Bundle();
        args.putInt(LocalCommentLoader.POST_AUTHOR_ID_BUNDLE_KEY, mPost.getUserId());
        args.putString(LocalCommentLoader.POST_ID_BUNDLE_KEY, mPost.getPostId());
        LocalCommentLoader localCommentLoader = new LocalCommentLoader(
                this,
                mAdapter);

        getLoaderManager().initLoader(0, args, localCommentLoader);

        // Swipe to refresh
        AsyncTaskLoader networkCommentsLoader = new NetworkCommentLoader(
                this,
                mPost.getUserId(),
                mPost.getPostId());

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);
        refreshLayout.setOnRefreshListener(() ->
        {
            networkCommentsLoader.forceLoad();
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
}
