package io.islnd.android.islnd.app.models;

public class PostAliasKey {
    private final String postAuthorAlias;
    private final String postId;

    public PostAliasKey(String postAuthorAlias, String postId) {
        this.postAuthorAlias = postAuthorAlias;
        this.postId = postId;
    }

    public String getPostAuthorAlias() {
        return postAuthorAlias;
    }

    public String getPostId() {
        return postId;
    }
}
