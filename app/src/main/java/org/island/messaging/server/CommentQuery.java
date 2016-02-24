package org.island.messaging.server;

public class CommentQuery {
    private static final String TAG = CommentQuery.class.getSimpleName();

    private final String postAuthorPseudonym;
    private final String postId;

    public CommentQuery(String postAuthorPseudonym, String postId) {
        this.postAuthorPseudonym = postAuthorPseudonym;
        this.postId = postId;
    }
}
