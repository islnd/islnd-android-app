package io.islnd.android.islnd.app.models;

import android.net.Uri;

import java.io.Serializable;

public class Profile implements Serializable, VersionedContent {

    private final String displayName;
    private final String aboutMe;
    private final Uri profileImageUri;
    private final Uri headerImageUri;
    private final int version;

    public Profile(
            String displayName,
            String aboutMe,
            Uri profileImageUri,
            Uri headerImageUri,
            int version)
    {
        this.displayName = displayName;
        this.aboutMe = aboutMe;
        this.profileImageUri = profileImageUri;
        this.headerImageUri = headerImageUri;
        this.version = version;
    }

    public String getDisplayName()
    {
        return displayName;
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

    public int getVersion() {
        return version;
    }
}
