package org.island.messaging;

import com.island.island.Models.CommentKey;
import com.island.island.Models.Comment;
import com.island.island.Models.PostKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentCollection {

    Map<PostKey, List<Comment>> postKeyToComments;
    Map<PostKey, List<CommentKey>> postKeyToCommentDeletions;

    public CommentCollection() {
        this.postKeyToComments = new HashMap<>();
        this.postKeyToCommentDeletions = new HashMap<>();
    }

    public void add(int postAuthorId, int commentAuthorId, CommentUpdate commentUpdate) {
        if (commentUpdate.isDeletion()) {
            deleteComment(
                    commentAuthorId,
                    commentUpdate.getCommentId(),
                    postAuthorId,
                    commentUpdate.getPostId());
        }
        else {
            addComment(postAuthorId, commentAuthorId, commentUpdate);
        }
    }

    private void addComment(int postAuthorId, int commentAuthorId, CommentUpdate commentUpdate) {
        final PostKey postKey = new PostKey(postAuthorId, commentUpdate.getPostId());
        final CommentKey commentKey = new CommentKey(commentAuthorId, commentUpdate.getCommentId());
        if (postKeyToCommentDeletions.containsKey(postKey)
                && postKeyToCommentDeletions.get(postKey).contains(commentKey)) {
            postKeyToCommentDeletions.get(postKey).remove(commentKey);
            return;
        }

        if (!postKeyToComments.containsKey(postKey)) {
            postKeyToComments.put(postKey, new ArrayList<>());
        }

        postKeyToComments.get(postKey).add(
                new Comment(
                        postAuthorId,
                        commentUpdate.getPostId(),
                        commentAuthorId,
                        commentUpdate.getCommentId(),
                        commentUpdate.getContent(),
                        commentUpdate.getTimestamp()));
    }

    private void deleteComment(int commentAuthorId, String commentId, int postAuthorId, String postId) {
        final CommentKey commentKey = new CommentKey(commentAuthorId, commentId);
        final PostKey postKey = new PostKey(postAuthorId, postId);
        if (postKeyToComments.containsKey(postKey)
                && postKeyToComments.get(postKey).contains(commentKey)) {
            postKeyToComments.get(postKey).remove(commentKey);
        }
        else {
            if (!postKeyToCommentDeletions.containsKey(postKey)) {
                postKeyToCommentDeletions.put(postKey, new ArrayList<>());
            }

            postKeyToCommentDeletions.get(postKey).add(commentKey);
        }
    }

    public Map<PostKey, List<Comment>> getCommentsGroupedByPostKey() {
        return postKeyToComments;
    }

    public Map<PostKey, List<CommentKey>> getDeletions() {
        return postKeyToCommentDeletions;
    }

    public List<Comment> getComments(int postAuthorId, String postId) {
        final PostKey postKey = new PostKey(postAuthorId, postId);
        if (!postKeyToComments.containsKey(postKey)) {
            return new ArrayList<>();
        }

        return postKeyToComments.get(postKey);
    }
}
