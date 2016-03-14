package io.islnd.android.islnd.app.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.database.IslndDb;
import io.islnd.android.islnd.app.models.ProfileWithImageData;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.app.VersionedContentBuilder;
import com.soundcloud.android.crop.Crop;

import java.io.File;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = getApplicationContext();

        Profile profile = DataUtils.getProfile(
                getApplicationContext(),
                Util.getUserId(getApplicationContext()));

        TextView userName = (TextView) findViewById(R.id.profile_user_name);
        profileImage = (ImageView) findViewById(R.id.profile_profile_image);
        headerImage = (ImageView) findViewById(R.id.profile_header_image);
        aboutMe = (EditText) findViewById(R.id.edit_profile_about_me);

        prevProfileImageUri = profile.getProfileImageUri();
        prevHeaderImageUri = profile.getHeaderImageUri();

        userName.setText(profile.getDisplayName());
        aboutMe.setText(profile.getAboutMe());

        ImageUtil.setProfileImageSampled(getApplicationContext(), profileImage,
                profile.getProfileImageUri());
        ImageUtil.setHeaderImageSampled(getApplicationContext(), headerImage,
                profile.getHeaderImageUri());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if (requestCode == SELECT_PROFILE_IMAGE) {
                Uri destination = Uri.fromFile(new File(getCacheDir(), "croppedProfile"));
                Crop.of(data.getData(), destination).asSquare().start(this, CROP_PROFILE_IMAGE);
            } else if (requestCode == SELECT_HEADER_IMAGE) {
                Uri destination = Uri.fromFile(new File(getCacheDir(), "croppedHeader"));
                Crop.of(data.getData(), destination).withAspect(
                        headerImage.getWidth(),
                        headerImage.getHeight()
                ).start(this, CROP_HEADER_IMAGE);
            } else if (requestCode == CROP_PROFILE_IMAGE) {
                profileImageUri = Crop.getOutput(data);
                ImageUtil.setProfileImageSampled(getApplicationContext(), profileImage,
                        profileImageUri);
            } else if (requestCode == CROP_HEADER_IMAGE) {
                headerImageUri = Crop.getOutput(data);
                ImageUtil.setHeaderImageSampled(getApplicationContext(), headerImage,
                        headerImageUri);
            }
        }
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

    public void saveProfile(View view) {
        String newAboutMeText = aboutMe.getText().toString();
        String myDisplayName = Util.getDisplayName(mContext);

        // TODO: Implement separate REST calls for profile/header images
        Uri newProfileUri = profileImageUri == null ? prevProfileImageUri : profileImageUri;
        Uri newHeaderUri = headerImageUri == null ? prevHeaderImageUri : headerImageUri;

        ProfileWithImageData newProfileWithImageData = VersionedContentBuilder.buildProfile(
                mContext,
                myDisplayName,
                newAboutMeText,
                ImageUtil.getByteArrayFromUri(getApplicationContext(), newProfileUri),
                ImageUtil.getByteArrayFromUri(getApplicationContext(), newHeaderUri)
        );

        // TODO: This saves a new image every time. Will change with new REST calls.
        Uri savedProfileImageUri = ImageUtil.saveBitmapToInternalFromUri(
                mContext,
                newProfileUri);
        Uri savedHeaderImageUri = ImageUtil.saveBitmapToInternalFromUri(
                mContext,
                newHeaderUri);

        Profile newProfile = new Profile(
                myDisplayName,
                newAboutMeText,
                savedProfileImageUri,
                savedHeaderImageUri,
                newProfileWithImageData.getVersion()
        );

        int myUserId = Util.getUserId(mContext);
        DataUtils.updateProfile(getApplicationContext(), newProfile, myUserId);
        IslndDb.postProfile(getApplicationContext(), newProfileWithImageData);

        Snackbar.make(view, getString(R.string.profile_saved), Snackbar.LENGTH_SHORT).show();
    }

    public void chooseProfileImage(View view) {
        Crop.pickImage(this, SELECT_PROFILE_IMAGE);
    }

    public void chooseHeaderImage(View view) {
        Crop.pickImage(this, SELECT_HEADER_IMAGE);
    }
}
