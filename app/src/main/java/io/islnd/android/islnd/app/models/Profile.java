package io.islnd.android.islnd.app.models;

import android.net.Uri;

import java.io.Serializable;

public class Profile implements Serializable {

    private final String aboutMe;
    private final Uri profileImageUri;
    private final Uri headerImageUri;

    public Profile(
            String aboutMe,
            Uri profileImageUri,
            Uri headerImageUri)
    {
        this.aboutMe = aboutMe;
        this.profileImageUri = profileImageUri;
        this.headerImageUri = headerImageUri;
    }

    public String getAboutMe()
    {
        return aboutMe;
    }

    public Uri getProfileImageUri() {
        return profileImageUri;
    }

    public Uri getHeaderImageUri() {
        return headerImageUri;
    }
}
