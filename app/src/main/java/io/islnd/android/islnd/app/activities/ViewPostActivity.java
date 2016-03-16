package io.islnd.android.islnd.app.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.islnd.android.islnd.app.DeletePostDialog;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.adapters.CommentAdapter;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndDb;
import io.islnd.android.islnd.app.loader.LocalCommentLoader;
import io.islnd.android.islnd.app.loader.NetworkCommentLoader;
import io.islnd.android.islnd.app.models.Post;
import io.islnd.android.islnd.app.SimpleDividerItemDecoration;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;

public class ViewPostActivity extends AppCompatActivity {

    private Post mPost = null;

    private RecyclerView mRecyclerView;
    private CommentAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = getApplicationContext();

        // Get intent with post info
        Intent intent = getIntent();
        mPost = (Post)intent.getSerializableExtra(Post.POST_EXTRA);
        bindPost();

        // List view stuff
        mRecyclerView = (RecyclerView) findViewById(R.id.view_post_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CommentAdapter(this, null, mPost.getKey());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        mRecyclerView.setNestedScrollingEnabled(false);

        // Load the local comments
        Bundle args = new Bundle();
        args.putInt(LocalCommentLoader.POST_AUTHOR_ID_BUNDLE_KEY, mPost.getUserId());
        args.putString(LocalCommentLoader.POST_ID_BUNDLE_KEY, mPost.getPostId());
        LocalCommentLoader localCommentLoader = new LocalCommentLoader(
                this,
                mAdapter);

        getSupportLoaderManager().initLoader(0, args, localCommentLoader);

        // Swipe to refresh
        AsyncTaskLoader networkCommentsLoader = new NetworkCommentLoader(
                this,
                mPost.getUserId(),
                mPost.getPostId());

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);
        mRefreshLayout.setOnRefreshListener(() ->
        {
            networkCommentsLoader.forceLoad();
            getApplicationContext().getContentResolver().requestSync(
                    Util.getSyncAccount(getApplicationContext()),
                    IslndContract.CONTENT_AUTHORITY,
                    new Bundle()
            );

            mRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            IslndDb.addCommentToPost(
                    this,
                    mPost,
                    commentText);

            //--Clear edit text
            addCommentEditText.setText("");
            imm.hideSoftInputFromWindow(addCommentEditText.getWindowToken(), 0);
        }
    }
    
    private void bindPost() {
        if (mPost == null) {
            return;
        }

        ImageView postProfileImage = (ImageView) findViewById(R.id.post_profile_image);
        TextView postUserName = (TextView) findViewById(R.id.post_user_name);
        TextView postTimestamp = (TextView) findViewById(R.id.post_timestamp);
        TextView postContent = (TextView) findViewById(R.id.post_content);
        RelativeLayout postOverflow  = (RelativeLayout) findViewById(R.id.post_overflow_layout);
        TextView commentCount = (TextView) findViewById(R.id.post_comment_count);
        
        postUserName.setText(mPost.getUserName());
        postTimestamp.setText(Util.smartTimestampFromUnixTime(mPost.getTimestamp()));
        postContent.setText(mPost.getContent());
        //TODO: Get actual comment count
        commentCount.setText(Util.numberOfCommentsString(0));

        // Go to profile on picture click
        postProfileImage.setOnClickListener((View v) -> {
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            profileIntent.putExtra(ProfileActivity.USER_ID_EXTRA, mPost.getUserId());
            startActivity(profileIntent);
        });

        Profile profile = DataUtils.getProfile(mContext, mPost.getUserId());
        Uri profileImageUri = profile.getProfileImageUri();
        ImageUtil.setPostProfileImageSampled(mContext, postProfileImage, profileImageUri);

        if(Util.isUser(mContext, mPost.getUserId())) {
            postOverflow.setVisibility(View.VISIBLE);

            postOverflow.setOnClickListener((View v) -> {
                PopupMenu popup = new PopupMenu(this, postOverflow);
                popup.getMenuInflater().inflate(R.menu.post_menu, popup.getMenu());
                popup.setOnMenuItemClickListener((MenuItem item) -> {
                    switch (item.getItemId()) {
                        case R.id.delete_post:
                            DeletePostDialog deletePostFragment =
                                    DeletePostDialog.buildWithArgs(mPost.getUserId(), mPost.getPostId());
                            deletePostFragment.show(
                                    getSupportFragmentManager(),
                                    mContext.getString(R.string.fragment_delete_post));
                            deletePostFragment.setFinishActivityIfSuccess(true);
                    }

                    return true;
                });

                popup.show();
            });
        } else {
            postOverflow.setVisibility(View.GONE);
        }
    }
}
