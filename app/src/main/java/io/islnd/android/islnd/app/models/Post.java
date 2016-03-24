package io.islnd.android.islnd.app.models;

import java.io.Serializable;

public class Post implements Serializable {
    private static final String TAG = Post.class.getSimpleName();

    public static String POST_EXTRA = "POST_OBJECT";

    private final String displayName;
    private final int userId;
    private final String alias;
    private final long timestamp;
    private final String content;
    private final String postId;
    private final int commentCount;

    public Post(String displayName,
                int userId,
                String alias,
                String postId,
                long timestamp,
                String content,
                int commentCount) {
        this.displayName = displayName;
        this.userId = userId;
        this.alias = alias;
        this.postId = postId;
        this.timestamp = timestamp;
        this.content = content;
        this.commentCount = commentCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    public PostKey getKey() {
        return new PostKey(this.userId, this.postId);
    }

    public String getPostId() {
        return postId;
    }

    public int getUserId() {
        return userId;
    }

    public String getAlias() {
        return alias;
    }

    public int getCommentCount() {
        return commentCount;
    }
}
