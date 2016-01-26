package com.island.island.Containers;

import java.io.Serializable;

/**
 * Created by David Thompson on 12/21/2015.
 *
 * This class represents a user post.
 */
public class Post implements Serializable
{
    // When passing post with intent
    public static String POST_EXTRA = "POST_OBJECT";

    public String profilePictureUri = "";
    public String profileName = "";
    public String timestamp = "";
    public String content = "";

    public Post(String profilePictureUri, String profileName, String timestamp, String content)
    {
        this.profilePictureUri = profilePictureUri;
        this.profileName = profileName;
        this.timestamp = timestamp;
        this.content = content;
    }
}
