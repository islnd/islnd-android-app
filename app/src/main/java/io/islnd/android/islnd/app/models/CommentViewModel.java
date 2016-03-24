package io.islnd.android.islnd.app.models;

import java.io.Serializable;

public class CommentViewModel implements Serializable {
    private final String username;
    private final int userId;
    private final String commentId;
    private final String comment;
    private final long timestamp;

    public CommentViewModel(String username, int userId, String commentId, String comment, long timestamp) {
        this.username = username;
        this.userId = userId;
        this.commentId = commentId;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public CommentViewModel(String username, int userId, String commentId, String commentText) {
        this.username = username;
        this.userId = userId;
        this.commentId = commentId;
        this.comment = commentText;
        this.timestamp = System.currentTimeMillis();
    }

    public String getComment() {
        return comment;
    }

    public String getUsername() {
        return username;
    }

    public CommentKey getKey() {
        return new CommentKey(userId, commentId);
    }

    public String getCommentId() {
        return commentId;
    }

    public int getUserId() {
        return userId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
