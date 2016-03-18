package io.islnd.android.islnd.app.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
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

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.adapters.PostAdapter;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.Dialogs;
import io.islnd.android.islnd.app.database.IslndDb;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.SimpleDividerItemDecoration;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();

    public static String USER_ID_EXTRA = "USER_ID";
    private static final int EDIT_PROFILE_REQUEST = 0;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mRefreshLayout;

    private int mProfileUserId;
    private Profile mProfile;
    private Cursor mPostCursor;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = getApplicationContext();

        Intent profileIntent = getIntent();
        mProfileUserId = profileIntent.getIntExtra(USER_ID_EXTRA, -1);
        mProfile = DataUtils.getProfile(mContext, mProfileUserId);
        showProfile();

        // Post list stuff
        mRecyclerView = (RecyclerView) findViewById(R.id.profile_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //--TODO this should use a loader
        String[] projection = new String[]{
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME,
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry._ID,
                IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_USER_ID,
                IslndContract.PostEntry.COLUMN_POST_ID,
                IslndContract.PostEntry.COLUMN_TIMESTAMP,
                IslndContract.PostEntry.COLUMN_CONTENT,
        };
        mPostCursor = getContentResolver().query(
                IslndContract.PostEntry.buildPostUriWithUserId(mProfileUserId),
                projection,
                null,
                null,
                null
        );

        mAdapter = new PostAdapter(this, mPostCursor);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPostCursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);

        if (Util.isUser(this, mProfileUserId)) {
            MenuItem removeFriend = menu.findItem(R.id.remove_friend);
            removeFriend.setVisible(false);
        } else {
            MenuItem editProfile = menu.findItem(R.id.edit_profile);
            editProfile.setVisible(false);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_PROFILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                mProfile = DataUtils.getProfile(mContext, mProfileUserId);
                showProfile();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.remove_friend:
                Dialogs.removeFriendDialog(this, mProfileUserId, mProfile.getDisplayName());
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
        if (mProfile == null) {
            return;
        }

        ImageView headerImage = (ImageView) findViewById(R.id.profile_header_image);
        ImageView profileImage = (ImageView) findViewById(R.id.profile_profile_image);
        TextView displayName = (TextView) findViewById(R.id.profile_display_name);
        TextView aboutMe = (TextView) findViewById(R.id.profile_about_me);
        View toolbarOverlay = findViewById(R.id.toolbar_overlay);
        AppBarLayout appBar= (AppBarLayout) findViewById(R.id.app_bar_layout);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);

        displayName.setText(mProfile.getDisplayName());
        aboutMe.setText(mProfile.getAboutMe());
        collapsingToolbar.setTitle(" ");
        ImageUtil.setProfileImageSampled(mContext, profileImage, mProfile.getProfileImageUri());
        ImageUtil.setHeaderImageSampled(mContext, headerImage, mProfile.getHeaderImageUri());

        appBar.addOnOffsetChangedListener((AppBarLayout appBarLayout, int verticalOffset) -> {
            mRefreshLayout.setEnabled(verticalOffset == 0);

            verticalOffset = Math.abs(verticalOffset);
            int scrollRange = appBarLayout.getTotalScrollRange();
            float threshold = (int) (scrollRange * 0.70f);
            float ratio = (float) verticalOffset / threshold;
            ratio = Math.max(0f, Math.min(1f, ratio));

            ViewCompat.setAlpha(profileImage, 1 - ratio);
            ViewCompat.setAlpha(displayName, 1 - ratio);
            ViewCompat.setAlpha(aboutMe, 1 - ratio);
            ViewCompat.setAlpha(toolbarOverlay, 1 - ratio);

            if (scrollRange - verticalOffset == 0) {
                collapsingToolbar.setTitle(mProfile.getDisplayName());
            } else {
                collapsingToolbar.setTitle(" ");
            }
        });

        profileImage.setOnClickListener((View view) -> {
            viewProfileImage();
        });

        mRefreshLayout.setOnRefreshListener(() -> {
            // TODO: Run async task again
            mRefreshLayout.setRefreshing(false);
        });
    }

    public void startNewPostActivity(View view) {
        Intent newPostIntent = new Intent(ProfileActivity.this, NewPostActivity.class);
        startActivity(newPostIntent);
    }

    private void viewProfileImage() {
        Intent intent = new Intent(this, ImageViewerActivity.class);

        intent.putExtra(ImageViewerActivity.IMAGE_VIEW_URI,
                mProfile.getProfileImageUri().toString());
        startActivity(intent);
    }

    public void viewHeaderImage(View view) {
        Intent intent = new Intent(this, ImageViewerActivity.class);

        intent.putExtra(ImageViewerActivity.IMAGE_VIEW_URI,
                mProfile.getHeaderImageUri().toString());
        startActivity(intent);
    }
}
