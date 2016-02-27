package com.island.island.Models;

public class Comment {
    private final int postUserId;
    private final String postId;
    private final int commentUserId;
    private final String content;
    private final long timestamp;

    public Comment(int postUserId, String postId, int commentUserId, String content, long timestamp) {
        this.postUserId = postUserId;
        this.postId = postId;
        this.commentUserId = commentUserId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public int getCommentUserId() {
        return commentUserId;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
