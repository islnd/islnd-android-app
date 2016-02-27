package com.island.island.Models;

import java.io.Serializable;

public class Comment implements Serializable
{
    private String userName;
    private String comment;
    private long timestamp;

    public Comment(String username, String comment, long timestamp)
    {
        this.userName = username;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public Comment(String username, String commentText) {
        this.userName = username;
        this.comment = commentText;
        this.timestamp = System.currentTimeMillis();
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }
}
