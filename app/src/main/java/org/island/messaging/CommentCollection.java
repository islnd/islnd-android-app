package org.island.messaging;

import com.island.island.Models.Comment;
import com.island.island.Models.Post;
import com.island.island.Models.RawComment;

import org.island.messaging.CommentUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentCollection {

    //--PostAuthorId->PostId->Comments
    HashMap<Integer, HashMap<String, List<RawComment>>> map;

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

        if (!map.get(postAuthorId).containsKey(commentUpdate.getPostId())) {
            map.get(postAuthorId).put(commentUpdate.getPostId(), new ArrayList<>());
        }

        map.get(postAuthorId).get(commentUpdate.getPostId()).add(
                new RawComment(
                        commentAuthorId,
                        commentUpdate.getContent(),
                        commentUpdate.getTimestamp()));
    }

    public List<RawComment> getComments(Post post) {
        return getComments(post.getUserId(), post.getPostId());
    }

    public List<RawComment> getComments(int postAuthorId, String postId) {
        if (!map.containsKey(postAuthorId)) {
            return new ArrayList<>();
        }

        if (!map.get(postAuthorId).containsKey(postId)) {
            return new ArrayList<>();
        }

        return map.get(postAuthorId).get(postId);
    }
}
