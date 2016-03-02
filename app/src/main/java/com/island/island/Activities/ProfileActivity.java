package com.island.island.Activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
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

import com.island.island.Adapters.PostAdapter;
import com.island.island.Database.IslndContract;
import com.island.island.Database.ProfileDatabase;
import com.island.island.Dialogs;
import com.island.island.Models.Post;
import com.island.island.Models.Profile;
import com.island.island.Database.IslandDB;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;
import com.island.island.Utils.ImageUtils;
import com.island.island.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    public static String USER_NAME_EXTRA = "USER_NAME";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout refreshLayout;

    private List<Post> mArrayOfPosts;
    private String mProfileUsername;
    private Profile mProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Post list stuff
        mArrayOfPosts = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.profile_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        String[] projection = new String[]{
                IslndContract.PostEntry.COLUMN_USER_ID,
                IslndContract.PostEntry.COLUMN_POST_ID,
                IslndContract.PostEntry.COLUMN_TIMESTAMP,
                IslndContract.PostEntry.COLUMN_CONTENT,
        };
        Cursor postCursor = getContentResolver().query(
                IslndContract.PostEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
        mAdapter = new PostAdapter(this, postCursor);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        Intent profileIntent = getIntent();
        mProfileUsername = profileIntent.getStringExtra(USER_NAME_EXTRA);
        mProfile = IslandDB.getProfile(getApplicationContext(), mProfileUsername);
        showProfile();
        new GetProfileTask().execute();
    }

    private void showProfile() {
        if (mProfile == null) {
            return;
        }
        ImageView headerImage = (ImageView) findViewById(R.id.profile_header_image);
        ImageView profileImage = (ImageView) findViewById(R.id.profile_profile_image);
        TextView aboutMe = (TextView) findViewById(R.id.profile_about_me);
        ImageView editProfile = (ImageView) findViewById(R.id.edit_profile_button);

        if(Utils.isUser(this, mProfileUsername)) {
            editProfile.setVisibility(View.VISIBLE);
            editProfile.setOnClickListener((View v) -> {
                startActivity(new Intent(this, EditProfileActivity.class));
            });
        }

        aboutMe.setText(mProfile.getAboutMe());
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mProfileUsername);
        Context context = getApplicationContext();
        ImageUtils.setProfileImageSampled(context, profileImage, mProfile.getProfileImageUri());
        ImageUtils.setHeaderImageSampled(context, headerImage, mProfile.getHeaderImageUri());

        // User posts
        // TODO get the real posts
//        List<Post> userPosts = IslandDB.getPostsForUser(new User(mProfileUsername));
//        mArrayOfPosts.addAll(userPosts);

        // Swipe to refresh
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh_layout);

        refreshLayout.setOnRefreshListener(() -> {
                    // TODO: Run async task again
                    refreshLayout.setRefreshing(false);
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);

        // If this is the client user's profile, don't show menu
        return !Utils.isUser(this, mProfileUsername);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.remove_friend) {
            Dialogs.removeFriendDialog(this, mProfileUsername);
            // TODO: What behavior do we want after removing friend?
            // Probably go back to feed.
        }

        return super.onOptionsItemSelected(item);
    }

    public void startNewPostActivity(View view) {
        Intent newPostIntent = new Intent(ProfileActivity.this, NewPostActivity.class);
        startActivity(newPostIntent);
    }

    public void viewProfileImage(View view) {
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

    private class GetProfileTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            mProfile = IslandDB.getMostRecentProfile(getApplicationContext(), mProfileUsername);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            showProfile();
        }
    }
}
