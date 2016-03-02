package com.island.island.Models;

public class Comment {
    private final int postUserId;
    private final String postId;
    private final int commentUserId;
    private final String commentId;
    private final String content;
    private final long timestamp;

    public Comment(int postUserId,
                   String postId,
                   int commentUserId,
                   String commentId,
                   String content,
                   long timestamp) {
        this.postUserId = postUserId;
        this.postId = postId;
        this.commentUserId = commentUserId;
        this.commentId = commentId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public int getCommentUserId() {
        return commentUserId;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public CommentKey getKey() {
        return new CommentKey(commentUserId, commentId);
    }
}
