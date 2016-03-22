package io.islnd.android.islnd.app.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
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

    private static final String PROFILE_IMAGE_URI_STATE = "PROFILE_IMAGE_URI_STATE";
    private static final String HEADER_IMAGE_URI_STATE = "HEADER_IMAGE_URI_STATE";

    public static final int LOADER_ID = 3;

    private static final int SELECT_PROFILE_IMAGE = 1;
    private static final int SELECT_HEADER_IMAGE = 2;
    private static final int CROP_PROFILE_IMAGE = 3;
    private static final int CROP_HEADER_IMAGE = 4;

    private TextInputEditText mDisplayNameEditText;
    private TextInputEditText mAboutMeEditText;
    private ImageView mProfileImageView;
    private ImageView mHeaderImageView;
    private Uri mNewProfileImageUri = null;
    private Uri mNewHeaderImageUri = null;
    private String mPreviousAboutMeText;
    private String mPreviousDisplayNameText;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = getApplicationContext();

        mDisplayNameEditText = (TextInputEditText) findViewById(R.id.edit_display_name);
        mAboutMeEditText = (TextInputEditText) findViewById(R.id.edit_profile_about_me);
        mProfileImageView = (ImageView) findViewById(R.id.profile_profile_image);
        mHeaderImageView = (ImageView) findViewById(R.id.profile_header_image);

        if (savedInstanceState != null) {
            String profileImageUriString = savedInstanceState.getString(PROFILE_IMAGE_URI_STATE);
            String headerImageUriString = savedInstanceState.getString(HEADER_IMAGE_URI_STATE);

            if (profileImageUriString != null) {
                mNewProfileImageUri = Uri.parse(profileImageUriString);
                ImageUtil.setProfileImageSampled(
                        mContext,
                        mProfileImageView,
                        mNewProfileImageUri);
            }

            if (headerImageUriString != null) {
                mNewHeaderImageUri = Uri.parse(headerImageUriString);
                ImageUtil.setHeaderImageSampled(
                        mContext,
                        mHeaderImageView,
                        mNewHeaderImageUri);
            }
        }

        getSupportLoaderManager().initLoader(LOADER_ID, new Bundle(), this);
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
                    ImageUtil.setProfileImageSampled(
                            mContext,
                            mProfileImageView,
                            mNewProfileImageUri);
                    break;
                case CROP_HEADER_IMAGE:
                    mNewHeaderImageUri = Crop.getOutput(data);
                    ImageUtil.setHeaderImageSampled(
                            mContext,
                            mHeaderImageView,
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mNewProfileImageUri != null) {
            savedInstanceState.putString(PROFILE_IMAGE_URI_STATE, mNewProfileImageUri.toString());
        }

        if (mNewHeaderImageUri != null) {
            savedInstanceState.putString(HEADER_IMAGE_URI_STATE, mNewHeaderImageUri.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    public void saveProfile(View view) {
        String newDisplayNameText = mDisplayNameEditText.getText().toString();
        String newAboutMeText = mAboutMeEditText.getText().toString();

        EventListBuilder profileEventList = new EventListBuilder(mContext);
        if (!newDisplayNameText.equals(mPreviousDisplayNameText)) {
            profileEventList.changeDisplayName(newDisplayNameText);
        }

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
        String newDisplayNameText = mDisplayNameEditText.getText().toString();
        String newAboutMeText = mAboutMeEditText.getText().toString();

        return !newDisplayNameText.equals(mPreviousDisplayNameText)
                || !newAboutMeText.equals(mPreviousAboutMeText)
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

        final String displayNameText = data.getString(data.getColumnIndex(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME));
        final String aboutMeText = data.getString(data.getColumnIndex(IslndContract.ProfileEntry.COLUMN_ABOUT_ME));
        mDisplayNameEditText.setText(displayNameText);
        mAboutMeEditText.setText(aboutMeText);
        mPreviousDisplayNameText = displayNameText;
        mPreviousAboutMeText = aboutMeText;

        if (mNewProfileImageUri == null) {
            ImageUtil.setProfileImageSampled(
                    this,
                    mProfileImageView,
                    Uri.parse(data.getString(data.getColumnIndex(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI))));
        }

        if (mNewHeaderImageUri == null) {
            ImageUtil.setHeaderImageSampled(
                    this,
                    mHeaderImageView,
                    Uri.parse(data.getString(data.getColumnIndex(IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI))));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
