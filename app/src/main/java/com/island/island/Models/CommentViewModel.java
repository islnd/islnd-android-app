package com.island.island.Models;

import java.io.Serializable;

public class CommentViewModel implements Serializable
{
    private final String username;
    private final String comment;
    private final long timestamp;

    public CommentViewModel(String username, String comment, long timestamp)
    {
        this.username = username;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public CommentViewModel(String username, String commentText) {
        this.username = username;
        this.comment = commentText;
        this.timestamp = System.currentTimeMillis();
    }

    public String getComment()
    {
        return comment;
    }

    public String getUsername()
    {
        return username;
    }

    public String getKey() {
        return username + timestamp;
    }
}
