package com.island.island.Models;

public class RawComment {
    private int commentUserId;
    private String content;
    private long timestamp;

    public RawComment(int commentUserId, String content, long timestamp) {
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
