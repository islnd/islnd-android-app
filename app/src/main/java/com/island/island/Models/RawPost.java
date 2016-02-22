package com.island.island.Models;

public class RawPost {
    private int userId;
    private String postId;
    private String content;
    private long timestamp;

    public RawPost(int userId, String postId, String content, long timestamp) {
        this.userId = userId;
        this.postId = postId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    public int getUserId() {
        return userId;
    }
}
