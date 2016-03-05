package io.islnd.android.islnd.app.models;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CommentKey {
    private final int commentAuthorId;
    private final String commentId;

    public CommentKey(int commentAuthorId, String commentId) {
        this.commentAuthorId = commentAuthorId;
        this.commentId = commentId;
    }

    public String getCommentId() {
        return commentId;
    }

    public int getCommentAuthorId() {
        return commentAuthorId;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CommentKey)) {
            return false;
        }

        CommentKey otherKey = (CommentKey) other;
        final boolean result = otherKey.getCommentId().equals(this.commentId)
                && otherKey.getCommentAuthorId() == this.commentAuthorId;
        return result;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(this.commentAuthorId)
                .append(this.commentId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return String.format("user id %d comment id %s", this.commentAuthorId, this.commentId);
    }
}
