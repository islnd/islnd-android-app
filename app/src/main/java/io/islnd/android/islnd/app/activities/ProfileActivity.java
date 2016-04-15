package io.islnd.android.islnd.app.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.islnd.android.islnd.app.Dialogs;
import io.islnd.android.islnd.app.IslndAction;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.SimpleDividerItemDecoration;
import io.islnd.android.islnd.app.StopRefreshReceiver;
import io.islnd.android.islnd.app.adapters.PostAdapter;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.loader.LoaderId;
import io.islnd.android.islnd.app.loader.PostLoader;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;

public class ProfileActivity extends IslndActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    public static final String USER_ID_EXTRA = "USER_ID";
    private static final int EDIT_PROFILE_REQUEST = 0;

    private RecyclerView mRecyclerView;
    private PostAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;
    private StopRefreshReceiver mStopRefreshReceiver;

    private int mProfileUserId;
    private String mDisplayName;

    private ImageView mHeaderImageView;
    private ImageView mProfileImageView;
    private TextView mDisplayNameTextView;
    private TextView mAboutMeTextView;

    private String mProfileImageUriString;
    private String mHeaderImageUriString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent profileIntent = getIntent();
        mProfileUserId = profileIntent.getIntExtra(USER_ID_EXTRA, -1);
        showProfile();

        // Post list stuff
        mRecyclerView = (RecyclerView) findViewById(R.id.profile_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PostAdapter(this, null);
        final PostLoader postLoader = new PostLoader(
                this,
                IslndContract.PostEntry.buildPostUriWithUserId(mProfileUserId),
                mAdapter);

        getSupportLoaderManager().initLoader(LoaderId.POST_LOADER_ID, new Bundle(), postLoader);
        getSupportLoaderManager().initLoader(LoaderId.PROFILE_ACTIVITY_LOADER_ID, new Bundle(), this);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);

        if (mProfileUserId == IslndContract.UserEntry.MY_USER_ID) {
            MenuItem removeFriend = menu.findItem(R.id.remove_friend);
            removeFriend.setVisible(false);
        } else {
            MenuItem editProfile = menu.findItem(R.id.edit_profile);
            editProfile.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.remove_friend:
                Dialogs.removeFriendDialog(this, mProfileUserId, mDisplayName);
                // TODO: What behavior do we want after removing friend?
                // Probably go back to feed.
                break;
            case R.id.edit_profile:
                startActivityForResult(
                        new Intent(this, EditProfileActivity.class),
                        EDIT_PROFILE_REQUEST
                );
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showProfile() {
        mHeaderImageView = (ImageView) findViewById(R.id.profile_header_image);
        mProfileImageView = (ImageView) findViewById(R.id.profile_profile_image);
        mDisplayNameTextView = (TextView) findViewById(R.id.profile_display_name);
        mAboutMeTextView = (TextView) findViewById(R.id.profile_about_me);

        View toolbarOverlay = findViewById(R.id.toolbar_overlay);
        AppBarLayout appBar= (AppBarLayout) findViewById(R.id.app_bar_layout);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);

        collapsingToolbar.setTitle(" ");


        appBar.addOnOffsetChangedListener(
                (AppBarLayout appBarLayout, int verticalOffset) -> {
                    mRefreshLayout.setEnabled(verticalOffset == 0);

                    verticalOffset = Math.abs(verticalOffset);
                    int scrollRange = appBarLayout.getTotalScrollRange();
                    float threshold = (int) (scrollRange * 0.70f);
                    float ratio = (float) verticalOffset / threshold;
                    ratio = Math.max(0f, Math.min(1f, ratio));

                    ViewCompat.setAlpha(mProfileImageView, 1 - ratio);
                    ViewCompat.setAlpha(mDisplayNameTextView, 1 - ratio);
                    ViewCompat.setAlpha(mAboutMeTextView, 1 - ratio);
                    ViewCompat.setAlpha(toolbarOverlay, 1 - ratio);

                    if (scrollRange - verticalOffset == 0) {
                        collapsingToolbar.setTitle(mDisplayName);
                    } else {
                        collapsingToolbar.setTitle(" ");
                    }
                });

        mProfileImageView.setOnClickListener(
                (View view) -> {
                    viewProfileImage();
                });

        mRefreshLayout.setOnRefreshListener(
                () -> {
                    getApplicationContext().getContentResolver().requestSync(
                            Util.getSyncAccount(getApplicationContext()),
                            IslndContract.CONTENT_AUTHORITY,
                            new Bundle()
                    );
                });
        mStopRefreshReceiver = new StopRefreshReceiver(mRefreshLayout);
    }

    public void startNewPostActivity(View view) {
        Intent newPostIntent = new Intent(ProfileActivity.this, NewPostActivity.class);
        startActivity(newPostIntent);
    }

    private void viewProfileImage() {
        Intent intent = new Intent(this, ImageViewerActivity.class);

        intent.putExtra(ImageViewerActivity.IMAGE_VIEW_URI,
                mProfileImageUriString);
        startActivity(intent);
    }

    public void viewHeaderImage(View view) {
        Intent intent = new Intent(this, ImageViewerActivity.class);

        intent.putExtra(ImageViewerActivity.IMAGE_VIEW_URI,
                mHeaderImageUriString);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(IslndAction.EVENT_SYNC_COMPLETE);
        this.registerReceiver(mStopRefreshReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mStopRefreshReceiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                IslndContract.ProfileEntry.TABLE_NAME + "." + IslndContract.PostEntry._ID,
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI,
                IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI,
                IslndContract.ProfileEntry.COLUMN_ABOUT_ME,
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME
        };

        return new CursorLoader(
                this,
                IslndContract.ProfileEntry.buildProfileUriWithUserId(mProfileUserId),
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

        mDisplayName = data.getString(data.getColumnIndex(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME));
        mDisplayNameTextView.setText(mDisplayName);

        mAboutMeTextView.setText(data.getString(data.getColumnIndex(IslndContract.ProfileEntry.COLUMN_ABOUT_ME)));

        mProfileImageUriString = data.getString(data.getColumnIndex(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI));
        ImageUtil.setProfileImageSampled(
                this,
                mProfileImageView,
                Uri.parse(mProfileImageUriString));

        mHeaderImageUriString = data.getString(data.getColumnIndex(IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI));
        ImageUtil.setHeaderImageSampled(
                this,
                mHeaderImageView,
                Uri.parse(mHeaderImageUriString));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
