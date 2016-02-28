package org.island.messaging;

import com.island.island.Models.Post;
import com.island.island.Models.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentCollection {

    //--PostAuthorId->PostId->Comments
    HashMap<Integer, HashMap<String, List<Comment>>> map;

    public CommentCollection() {
        this.map = new HashMap<>();
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

        return map.get(postAuthorId).get(postId);
    }
}
