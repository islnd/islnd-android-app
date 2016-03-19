package io.islnd.android.islnd.app.activities;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;

import io.islnd.android.islnd.app.DeletePostDialog;
import io.islnd.android.islnd.app.EventPushService;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.adapters.CommentAdapter;
import io.islnd.android.islnd.app.database.IslndDb;
import io.islnd.android.islnd.app.loader.LocalCommentLoader;
import io.islnd.android.islnd.app.models.Post;
import io.islnd.android.islnd.app.SimpleDividerItemDecoration;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.event.Event;
import io.islnd.android.islnd.messaging.event.EventListBuilder;
import io.islnd.android.islnd.messaging.event.EventProcessor;

public class ViewPostActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ViewPostActivity.class.getSimpleName();

    private Post mPost = null;

    private RecyclerView mRecyclerView;
    private CommentAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;
    private Context mContext;

    private ImageView mPostProfileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = this;

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
        getSupportLoaderManager().initLoader(1, new Bundle(), this);

        // Swipe to refresh
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);
        mRefreshLayout.setOnRefreshListener(() ->
        {
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
            List<Event> makeCommentEvents = new EventListBuilder(mContext)
                    .makeComment(
                            mPost.getPostId(),
                            DataUtils.getMostRecentAlias(mContext, mPost.getUserId()),
                            commentText )
                    .build();

            for (Event event : makeCommentEvents) {
                EventProcessor.process(mContext, event);
                Intent pushEventService = new Intent(mContext, EventPushService.class);
                pushEventService.putExtra(EventPushService.EVENT_EXTRA, event);
                mContext.startService(pushEventService);
            }

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
        mPostProfileImageView.setOnClickListener(
                (View v) -> {
                    Intent profileIntent = new Intent(this, ProfileActivity.class);
                    profileIntent.putExtra(ProfileActivity.USER_ID_EXTRA, mPost.getUserId());
                    startActivity(profileIntent);
                });


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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(TAG, "create loader " + id);
        String[] projection = new String[]{
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI,
        };

        return new CursorLoader(
                this,
                IslndContract.ProfileEntry.buildProfileUriWithUserId(mPost.getUserId()),
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        ImageUtil.setPostProfileImageSampled(
                mContext,
                mPostProfileImageView,
                Uri.parse(data.getString(data.getColumnIndex(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI))));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
