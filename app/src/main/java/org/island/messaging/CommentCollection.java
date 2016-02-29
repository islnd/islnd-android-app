package org.island.messaging;

import com.island.island.Models.CommentKey;
import com.island.island.Models.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommentCollection {

    //--PostAuthorId->PostId->Comments
    HashMap<Integer, HashMap<String, List<Comment>>> map;
    private Set<CommentKey> deletedKeys;

    public CommentCollection() {
        this.map = new HashMap<>();
        this.deletedKeys = new HashSet<>();
    }

    public void add(int postAuthorId, int commentAuthorId, CommentUpdate commentUpdate) {
        if (commentUpdate.isDeletion()) {
            return;
        }

        if (!map.containsKey(postAuthorId)) {
            map.put(postAuthorId, new HashMap<>());
        }

        final String postId = commentUpdate.getPostId();
        if (!map.get(postAuthorId).containsKey(postId)) {
            map.get(postAuthorId).put(postId, new ArrayList<>());
        }

        map.get(postAuthorId).get(postId).add(
                new Comment(
                        postAuthorId,
                        postId,
                        commentAuthorId,
                        commentUpdate.getCommentId(),
                        commentUpdate.getContent(),
                        commentUpdate.getTimestamp()));
    }

    public List<Comment> getComments(int postAuthorId, String postId) {
        if (!map.containsKey(postAuthorId)) {
            return new ArrayList<>();
        }

        if (!map.get(postAuthorId).containsKey(postId)) {
            return new ArrayList<>();
        }

        List<Comment> comments = new ArrayList<>();
        for (Comment comment : map.get(postAuthorId).get(postId)) {
            if (!deletedKeys.contains(comment.getKey())) {
                comments.add(comment);
            }
        }

        return comments;
    }

    public List<CommentKey> getDeletions(int userId, String postId) {
        //--TODO we should only return the keys associated with the particular post
        List<CommentKey> commentKeys = new ArrayList<>();
        for (CommentKey commentKey : deletedKeys) {
            commentKeys.add(commentKey);
        }

        return commentKeys;
    }

    public void addDelete(int commentAuthorId, String commentId) {
        CommentKey commentKey = new CommentKey(commentAuthorId, commentId);
        deletedKeys.add(commentKey);
    }
}
