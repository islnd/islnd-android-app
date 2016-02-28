package com.island.island.Models;

import android.util.Log;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PostKey {
    private final int userId;
    private final String postId;

    public PostKey(int userId, String postId) {
        this.userId = userId;
        this.postId = postId;
    }

    private int getUserId() {
        return this.userId;
    }

    public String getPostId() {
        return this.postId;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PostKey)) {
            return false;
        }

        PostKey otherKey = (PostKey) other;
        final boolean result = otherKey.getPostId().equals(this.postId)
                && otherKey.getUserId() == this.userId;
        return result;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(userId)
                .append(postId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return String.format("user %d post %s", this.userId, this.postId);
    }
}
