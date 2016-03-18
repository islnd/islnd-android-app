package io.islnd.android.islnd.app.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import io.islnd.android.islnd.app.EventPushService;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.database.IslndDb;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.app.VersionedContentBuilder;
import io.islnd.android.islnd.messaging.event.Event;
import io.islnd.android.islnd.messaging.event.EventListBuilder;
import io.islnd.android.islnd.messaging.event.EventProcessor;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {
    private static String TAG = EditProfileActivity.class.getSimpleName();

    private EditText aboutMe;
    private ImageView profileImage;
    private ImageView headerImage;

    private Uri prevProfileImageUri = null;
    private Uri prevHeaderImageUri = null;
    private Uri profileImageUri = null;
    private Uri headerImageUri = null;

    private static final int SELECT_PROFILE_IMAGE = 1;
    private static final int SELECT_HEADER_IMAGE = 2;
    private static final int CROP_PROFILE_IMAGE = 3;
    private static final int CROP_HEADER_IMAGE = 4;
    private Context mContext;
    private Profile mProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = getApplicationContext();

        mProfile = DataUtils.getProfile(
                getApplicationContext(),
                Util.getUserId(getApplicationContext()));

        TextView userName = (TextView) findViewById(R.id.profile_user_name);
        profileImage = (ImageView) findViewById(R.id.profile_profile_image);
        headerImage = (ImageView) findViewById(R.id.profile_header_image);
        aboutMe = (EditText) findViewById(R.id.edit_profile_about_me);

        prevProfileImageUri = mProfile.getProfileImageUri();
        prevHeaderImageUri = mProfile.getHeaderImageUri();

        userName.setText(mProfile.getDisplayName());
        aboutMe.setText(mProfile.getAboutMe());

        ImageUtil.setProfileImageSampled(getApplicationContext(), profileImage,
                mProfile.getProfileImageUri());
        ImageUtil.setHeaderImageSampled(getApplicationContext(), headerImage,
                mProfile.getHeaderImageUri());
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
                            headerImage.getWidth(),
                            headerImage.getHeight()
                    ).start(this, CROP_HEADER_IMAGE);
                    break;
                case CROP_PROFILE_IMAGE:
                    profileImageUri = Crop.getOutput(data);
                    ImageUtil.setProfileImageSampled(getApplicationContext(), profileImage,
                            profileImageUri);
                    break;
                case CROP_HEADER_IMAGE:
                    headerImageUri = Crop.getOutput(data);
                    ImageUtil.setHeaderImageSampled(getApplicationContext(), headerImage,
                            headerImageUri);
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
        String newAboutMeText = aboutMe.getText().toString();

        EventListBuilder profileEventList = new EventListBuilder(mContext);
        if (!newAboutMeText.equals(mProfile.getAboutMe())) {
            profileEventList.changeAboutMe(newAboutMeText);
        }

        if (profileImageUri != null) {
            profileEventList.changeProfileImage(profileImageUri);
        }

        if (headerImageUri != null) {
            profileEventList.changeHeaderImage(headerImageUri);
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
        String newAboutMeText = aboutMe.getText().toString();

        return !newAboutMeText.equals(mProfile.getAboutMe())
                || profileImageUri != null
                || headerImageUri != null;
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
}
