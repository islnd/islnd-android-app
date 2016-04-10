package io.islnd.android.islnd.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.islnd.android.islnd.app.DeletePostDialog;
import io.islnd.android.islnd.app.EventPublisher;
import io.islnd.android.islnd.app.IslndIntent;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.SimpleDividerItemDecoration;
import io.islnd.android.islnd.app.StopRefreshReceiver;
import io.islnd.android.islnd.app.adapters.CommentAdapter;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.loader.CommentLoader;
import io.islnd.android.islnd.app.loader.LoaderId;
import io.islnd.android.islnd.app.models.Post;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;

public class ViewPostActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ViewPostActivity.class.getSimpleName();

    private Post mPost = null;
    private String mPostId;
    private int mPostUserId;
    private Uri mPostProfileImageUri;

    private RecyclerView mRecyclerView;
    private CommentAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;
    private Context mContext;

    private ImageView mPostProfileImageView;
    private StopRefreshReceiver mStopRefreshReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = this;

        // Get intent with post info
        Intent intent = getIntent();
        mPostId = intent.getStringExtra(Post.POST_ID_EXTRA);
        mPostUserId = intent.getIntExtra(Post.POST_USER_ID_EXTRA, -1);

        getSupportLoaderManager().initLoader(LoaderId.VIEW_POST_ACTIVITY_LOADER_ID, new Bundle(), this);

        // Swipe to refresh
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);
        mRefreshLayout.setOnRefreshListener(() ->
        {
            getApplicationContext().getContentResolver().requestSync(
                    Util.getSyncAccount(getApplicationContext()),
                    IslndContract.CONTENT_AUTHORITY,
                    new Bundle()
            );
        });
        mStopRefreshReceiver = new StopRefreshReceiver(mRefreshLayout);
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

    public void addCommentToPost(View view) {
        EditText addCommentEditText = (EditText) findViewById(R.id.post_comment_edit_text);
        String commentText = addCommentEditText.getText().toString();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (commentText.equals("")) {
            Snackbar.make(view, getString(R.string.empty_comment_post), Snackbar.LENGTH_SHORT).show();
        } else {
            new EventPublisher(mContext)
                    .makeComment(
                            mPost.getPostId(),
                            mPost.getAlias(),
                            commentText)
                    .publish();

            //--Clear edit text
            addCommentEditText.setText("");
            imm.hideSoftInputFromWindow(addCommentEditText.getWindowToken(), 0);
        }
    }

    private void bindPost() {
        if (mPost == null) {
            return;
        }

        mPostProfileImageView = (ImageView) findViewById(R.id.post_profile_image);
        ImageUtil.setPostProfileImageSampled(
                mContext,
                mPostProfileImageView,
                mPostProfileImageUri);

        TextView postUserName = (TextView) findViewById(R.id.post_user_name);
        TextView postTimestamp = (TextView) findViewById(R.id.post_timestamp);
        TextView postContent = (TextView) findViewById(R.id.post_content);
        RelativeLayout postOverflow = (RelativeLayout) findViewById(R.id.post_overflow_layout);
        TextView commentCount = (TextView) findViewById(R.id.post_comment_count);

        postUserName.setText(mPost.getDisplayName());
        postTimestamp.setText(Util.smartTimestampFromUnixTime(mContext, mPost.getTimestamp()));
        postContent.setText(mPost.getContent());
        //TODO: Need to use loader to have dynamic comment count
        commentCount.setText("");

        // Go to profile on picture click
        mPostProfileImageView.setOnClickListener(
                (View v) -> {
                    Intent profileIntent = new Intent(this, ProfileActivity.class);
                    profileIntent.putExtra(ProfileActivity.USER_ID_EXTRA, mPost.getUserId());
                    startActivity(profileIntent);
                });


        if (mPost.getUserId() == IslndContract.UserEntry.MY_USER_ID) {
            postOverflow.setVisibility(View.VISIBLE);

            postOverflow.setOnClickListener((View v) -> {
                PopupMenu popup = new PopupMenu(this, postOverflow);
                popup.getMenuInflater().inflate(R.menu.post_menu, popup.getMenu());
                popup.setOnMenuItemClickListener((MenuItem item) -> {
                    switch (item.getItemId()) {
                        case R.id.delete_post:
                            DeletePostDialog deletePostFragment =
                                    DeletePostDialog.buildWithArgs(mPost.getPostId());
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(TAG, "create loader " + id);
        String[] projection = new String[]{
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_USER_ID,
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_ALIAS,
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_POST_ID,
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_TIMESTAMP,
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_CONTENT,
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_COMMENT_COUNT,
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI,
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME
        };

        return new CursorLoader(
                this,
                IslndContract.PostEntry.buildPostUriWithUserIdAndPostId(mPostUserId, mPostId),
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!cursor.moveToFirst()) {
            Toast.makeText(this, getString(R.string.post_does_not_exist), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mPost = new Post(
                cursor.getString(cursor.getColumnIndex(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME)),
                cursor.getInt(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_USER_ID)),
                cursor.getString(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_ALIAS)),
                cursor.getString(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_POST_ID)),
                cursor.getLong(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_TIMESTAMP)),
                cursor.getString(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_CONTENT)),
                cursor.getInt(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_COMMENT_COUNT)));

        mPostProfileImageUri = Uri.parse(cursor.getString(cursor.getColumnIndex(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI)));
        bindPost();

        // Comments
        mRecyclerView = (RecyclerView) findViewById(R.id.view_post_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CommentAdapter(this, null, mPost.getKey());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        mRecyclerView.setNestedScrollingEnabled(false);

        Bundle args = new Bundle();
        args.putString(CommentLoader.POST_AUTHOR_ALIAS_BUNDLE_KEY, mPost.getAlias());
        args.putString(CommentLoader.POST_ID_BUNDLE_KEY, mPost.getPostId());
        CommentLoader localCommentLoader = new CommentLoader(
                this,
                mAdapter);

        getSupportLoaderManager().initLoader(LoaderId.COMMENT_LOADER_ID, args, localCommentLoader);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(IslndIntent.EVENT_SYNC_COMPLETE);
        this.registerReceiver(mStopRefreshReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mStopRefreshReceiver);
    }
}
