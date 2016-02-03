package com.island.island.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.island.island.Adapters.ViewPostAdapter;
import com.island.island.Containers.Comment;
import com.island.island.Containers.Post;
import com.island.island.Database.IslandDB;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;


public class ViewPostActivity extends AppCompatActivity
{
    private Post post = null;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Boilerplate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // List view stuff
        ArrayList viewPostList = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.view_post_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ViewPostAdapter(viewPostList, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        // Get intent with post info
        Intent intent = getIntent();
        post = (Post)intent.getSerializableExtra(Post.POST_EXTRA);
        viewPostList.add(post);

        // Add comments to list
        List<Comment> comments = post.getComments();
        for(int i = 0; i < comments.size(); ++i)
        {
            viewPostList.add(comments.get(i));
        }
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
            IslandDB.addCommentToPost(post, new Comment("", commentText));
            addCommentEditText.setText("");
            imm.hideSoftInputFromWindow(addCommentEditText.getWindowToken(), 0);
        }
    }
}
