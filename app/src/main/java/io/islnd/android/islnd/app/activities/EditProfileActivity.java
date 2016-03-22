package io.islnd.android.islnd.app.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import io.islnd.android.islnd.app.EventPushService;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.messaging.event.Event;
import io.islnd.android.islnd.messaging.event.EventListBuilder;
import io.islnd.android.islnd.messaging.event.EventProcessor;

import com.soundcloud.android.crop.Crop;

import java.io.File;

public class EditProfileActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static String TAG = EditProfileActivity.class.getSimpleName();

    private EditText mAboutMeEditText;
    private ImageView mProfileImageView;
    private ImageView mHeaderImageView;
    private TextView mUserNameTextView;

    private Uri mNewProfileImageUri = null;
    private Uri mNewHeaderImageUri = null;

    private static final int SELECT_PROFILE_IMAGE = 1;
    private static final int SELECT_HEADER_IMAGE = 2;
    private static final int CROP_PROFILE_IMAGE = 3;
    private static final int CROP_HEADER_IMAGE = 4;

    private Context mContext;
    private String mPreviousAboutMeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = getApplicationContext();

        mUserNameTextView = (TextView) findViewById(R.id.profile_user_name);
        mProfileImageView = (ImageView) findViewById(R.id.profile_profile_image);
        mHeaderImageView = (ImageView) findViewById(R.id.profile_header_image);
        mAboutMeEditText = (EditText) findViewById(R.id.edit_profile_about_me);

        getSupportLoaderManager().initLoader(0, new Bundle(), this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_PROFILE_IMAGE:
                    Uri destination = Uri.fromFile(new File(getCacheDir(), "croppedProfile"));
                    Crop.of(data.getData(), destination).asSquare().start(this, CROP_PROFILE_IMAGE);
                    break;
                case SELECT_HEADER_IMAGE:
                    destination = Uri.fromFile(new File(getCacheDir(), "croppedHeader"));
                    Crop.of(data.getData(), destination).withAspect(
                            mHeaderImageView.getWidth(),
                            mHeaderImageView.getHeight()
                    ).start(this, CROP_HEADER_IMAGE);
                    break;
                case CROP_PROFILE_IMAGE:
                    mNewProfileImageUri = Crop.getOutput(data);
                    ImageUtil.setProfileImageSampled(getApplicationContext(), mProfileImageView,
                            mNewProfileImageUri);
                    break;
                case CROP_HEADER_IMAGE:
                    mNewHeaderImageUri = Crop.getOutput(data);
                    ImageUtil.setHeaderImageSampled(getApplicationContext(), mHeaderImageView,
                            mNewHeaderImageUri);
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (hasProfileChanged()) {
                    showDiscardChangesDialog();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasProfileChanged()) {
            showDiscardChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    public void saveProfile(View view) {
        String newAboutMeText = mAboutMeEditText.getText().toString();

        EventListBuilder profileEventList = new EventListBuilder(mContext);
        if (!newAboutMeText.equals(mPreviousAboutMeText)) {
            profileEventList.changeAboutMe(newAboutMeText);
        }

        if (mNewProfileImageUri != null) {
            profileEventList.changeProfileImage(mNewProfileImageUri);
        }

        if (mNewHeaderImageUri != null) {
            profileEventList.changeHeaderImage(mNewHeaderImageUri);
        }

        for (Event event : profileEventList.build()) {
            EventProcessor.process(mContext, event);

            Intent pushEventService = new Intent(mContext, EventPushService.class);
            pushEventService.putExtra(EventPushService.EVENT_EXTRA, event);
            mContext.startService(pushEventService);
        }

        setResult(RESULT_OK, null);
        finish();
    }

    private boolean hasProfileChanged() {
        String newAboutMeText = mAboutMeEditText.getText().toString();

        return !newAboutMeText.equals(mPreviousAboutMeText)
                || mNewProfileImageUri != null
                || mNewHeaderImageUri != null;
    }

    public void chooseProfileImage(View view) {
        Crop.pickImage(this, SELECT_PROFILE_IMAGE);
    }

    public void chooseHeaderImage(View view) {
        Crop.pickImage(this, SELECT_HEADER_IMAGE);
    }

    private void showDiscardChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setMessage(getString(R.string.discard_profile_changes_dialog))
                .setPositiveButton(R.string.discard, (DialogInterface dialog, int id) ->
                {
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
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
                IslndContract.ProfileEntry.buildProfileUriWithUserId(IslndContract.UserEntry.MY_USER_ID),
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

        mUserNameTextView.setText(data.getString(data.getColumnIndex(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME)));
        final String aboutMeText = data.getString(data.getColumnIndex(IslndContract.ProfileEntry.COLUMN_ABOUT_ME));
        mAboutMeEditText.setText(aboutMeText);
        mPreviousAboutMeText = aboutMeText;

        ImageUtil.setProfileImageSampled(
                this,
                mProfileImageView,
                Uri.parse(data.getString(data.getColumnIndex(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI))));

        ImageUtil.setHeaderImageSampled(
                this,
                mHeaderImageView,
                Uri.parse(data.getString(data.getColumnIndex(IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI))));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
