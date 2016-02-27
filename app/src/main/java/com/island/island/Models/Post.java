package com.island.island.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Thompson on 12/21/2015.
 *
 * This class represents a user post.
 */
public class Post implements Serializable
{
    public static String POST_EXTRA = "POST_OBJECT";

    private String userName = "";
    private long timestamp;
    private String content = "";
    private String postId = "";
    private List<Comment> comments = new ArrayList<>();

    public Post(String userName, String postId, long timestamp, String content,
                List<Comment> comments)
    {
        this.userName = userName;
        this.postId = postId;
        this.timestamp = timestamp;
        this.content = content;
        this.comments = comments;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public List<Comment> getComments()
    {
        return comments;
    }

    public void setComments(List<Comment> comments)
    {
        this.comments = comments;
    }

    public String getKey() {
        return getUserName() + getTimestamp();
    }

    public String getPostId() {
        return postId;
    }

    public boolean addComments(List<Comment> newComments) {
        boolean addedAny = false;
        for (Comment comment : newComments) {
            if (!hasComment(comment.getKey())) {
                this.comments.add(comment);
                addedAny = true;
            }
        }

        return addedAny;
    }

    private boolean hasComment(String key) {
        for (Comment comment : this.comments) {
            if (comment.getKey().equals(key)) {
                return true;
            }
        }

        return false;
    }
}
