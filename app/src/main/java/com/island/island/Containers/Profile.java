package com.island.island.Containers;

import java.io.Serializable;

/**
 * Created by David Thompson on 12/21/2015.
 *
 * This class represents a user profile.
 */
public class Profile implements Serializable
{
    public static String PROFILE_EXTRA = "PROFILE_OBJECT";

    public String headerPictureUri = "";
    public String profilePictureUri = "";
    public String userName = "";
    public String aboutMe = "";

    public Profile(String headerPictureUri, String profilePictureUri, String userName,
                   String aboutMe)
    {
        this.headerPictureUri = headerPictureUri;
        this.profilePictureUri = profilePictureUri;
        this.userName = userName;
        this.aboutMe = aboutMe;
    }
}
