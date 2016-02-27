package com.island.island.Models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by David Thompson on 12/21/2015.
 *
 * This class represents a user post.
 */
public class Post implements Serializable
{
    public static String POST_EXTRA = "POST_OBJECT";

    private final String userName;
    private final int userId;
    private final long timestamp;
    private final String content;
    private final String postId;
    private final List<CommentViewModel> comments;

    public Post(String userName, int userId, String postId, long timestamp, String content,
                List<CommentViewModel> comments)
    {
        this.userName = userName;
        this.userId = userId;
        this.postId = postId;
        this.timestamp = timestamp;
        this.content = content;
        this.comments = comments;
    }

    public String getUserName()
    {
        return userName;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public String getContent()
    {
        return content;
    }


    public List<CommentViewModel> getComments()
    {
        return comments;
    }

    public String getKey() {
        return getUserName() + getTimestamp();
    }

    public String getPostId() {
        return postId;
    }

    public boolean addComments(List<CommentViewModel> newComments) {
        boolean addedAny = false;
        for (CommentViewModel comment : newComments) {
            if (!hasComment(comment.getKey())) {
                this.comments.add(comment);
                addedAny = true;
            }
        }

        return addedAny;
    }

    private boolean hasComment(String key) {
        for (CommentViewModel comment : this.comments) {
            if (comment.getKey().equals(key)) {
                return true;
            }
        }

        return false;
    }

    public int getUserId() {
        return userId;
    }
}
