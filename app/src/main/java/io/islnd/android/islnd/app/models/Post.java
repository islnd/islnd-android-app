package io.islnd.android.islnd.app.models;

import android.util.Log;

import java.io.Serializable;
import java.util.List;

/**
 * Created by David Thompson on 12/21/2015.
 *
 * This class represents a user post.
 */
public class Post implements Serializable
{
    private static final String TAG = Post.class.getSimpleName();

    public static String POST_EXTRA = "POST_OBJECT";

    private final String userName;
    private final int userId;
    private final long timestamp;
    private final String content;
    private final String postId;
    private final List<CommentViewModel> comments;

    public Post(String userName,
                int userId,
                String postId,
                long timestamp,
                String content,
                List<CommentViewModel> comments)
    {
        this.userName = userName;
        this.userId = userId;
        this.postId = postId;
        this.timestamp = timestamp;
        this.content = content;
        this.comments = comments;
    }

    public String getUserName()
    {
        return userName;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public String getContent()
    {
        return content;
    }

    public List<CommentViewModel> getComments()
    {
        return comments;
    }

    public PostKey getKey() {
        return new PostKey(this.userId, this.postId);
    }

    public String getPostId() {
        return postId;
    }

    public boolean addComment(CommentViewModel comment) {
        if (!hasComment(comment.getKey())) {
            this.comments.add(comment);
            return true;
        }

        return false;
    }

    public boolean addComments(List<CommentViewModel> newComments) {
        boolean addedAny = false;
        for (CommentViewModel comment : newComments) {
            if (!hasComment(comment.getKey())) {
                this.comments.add(comment);
                addedAny = true;
            }
        }

        return addedAny;
    }

    private boolean hasComment(CommentKey key) {
        for (CommentViewModel comment : this.comments) {
            if (comment.getKey().equals(key)) {
                return true;
            }
        }

        return false;
    }

    public int getUserId() {
        return userId;
    }

    public boolean deleteComments(List<CommentKey> keysToDelete) {
        boolean anyDeleted = false;
        for (CommentKey keyToDelete : keysToDelete) {
            for (CommentViewModel comment : comments) {
                if (comment.getKey().equals(keyToDelete)) {
                    comments.remove(comment);
                    anyDeleted = true;
                    break;
                }
            }
        }

        return anyDeleted;
    }

    public void deleteComment(CommentKey keyToDelete) {
        for (CommentViewModel comment : comments) {
            if (comment.getKey().equals(keyToDelete)) {
                Log.v(TAG, "comment removed");
                comments.remove(comment);
                break;
            }
        }
    }
}
