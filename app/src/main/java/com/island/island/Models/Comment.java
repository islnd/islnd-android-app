package com.island.island.Models;

import java.io.Serializable;

public class Comment implements Serializable
{
    private String username;
    private String comment;
    private long timestamp;

    public Comment(String username, String comment, long timestamp)
    {
        this.username = username;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public Comment(String username, String commentText) {
        this.username = username;
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

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getKey() {
        return username + timestamp;
    }
}
