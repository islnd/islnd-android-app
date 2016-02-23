package com.island.island.Models;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by David Thompson on 12/21/2015.
 *
 * This class represents a user profile.
 */
public class Profile implements Serializable, VersionedContent {

    private String username = "";
    private String aboutMe = "";
    private Uri profileImageUri;
    private Uri headerImageUri;
    private int version;

    public Profile(String username, String aboutMe, Uri profileImageUri, Uri headerImageUri,
                   int version)
    {
        this.username = username;
        this.aboutMe = aboutMe;
        this.profileImageUri = profileImageUri;
        this.headerImageUri = headerImageUri;
        this.version = version;
    }

    public String getUsername()
    {
        return username;
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
