package com.island.island.Containers;

import java.io.Serializable;

/**
 * Created by David Thompson on 12/21/2015.
 *
 * This class represents a user post.
 */
public class Post implements Serializable
{
    public static String POST_EXTRA = "POST_OBJECT";

    private String userName = "";
    private String timestamp = "";
    private String content = "";
    private String profileImageUri = "";

    public Post(String userName, String timestamp, String content, String profileImageUri)
    {
        this.userName = userName;
        this.timestamp = timestamp;
        this.content = content;
        this.profileImageUri = profileImageUri;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getProfileImageUri()
    {
        return profileImageUri;
    }

    public void setProfileImageUri(String profileImageUri)
    {
        this.profileImageUri = profileImageUri;
    }
}
