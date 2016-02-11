package com.island.island.Models;

import java.io.Serializable;

/**
 * Created by David Thompson on 12/21/2015.
 *
 * This class represents a user profile.
 */
public class Profile implements Serializable
{
    public static String PROFILE_EXTRA = "PROFILE_OBJECT";

    private String userName = "";
    private String aboutMe = "";

    public Profile(String userName, String aboutMe)
    {
        this.userName = userName;
        this.aboutMe = aboutMe;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getAboutMe()
    {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe)
    {
        this.aboutMe = aboutMe;
    }

}
