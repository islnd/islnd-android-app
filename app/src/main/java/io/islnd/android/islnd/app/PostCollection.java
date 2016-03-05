package io.islnd.android.islnd.app;

import io.islnd.android.islnd.app.models.Post;
import io.islnd.android.islnd.app.models.PostKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostCollection {
    private List<Post> posts;
    private Set<PostKey> deletedKeys;

    public PostCollection() {
        this.posts = new ArrayList<>();
        this.deletedKeys = new HashSet<>();
    }

    public List<Post> getPosts() {
        List<Post> postsNotDeleted = new ArrayList<>();
        for (Post post : posts) {
            if (!isDeleted(post)) {
                postsNotDeleted.add(post);
            }
        }

        return postsNotDeleted;
    }

    private boolean isDeleted(Post post) {
        return deletedKeys.contains(post.getKey());
    }

    public PostKey[] getDeletedKeys() {
        PostKey[] deleted = new PostKey[deletedKeys.size()];
        deletedKeys.toArray(deleted);
        return deleted;
    }

    public void addPost(Post post) {
        posts.add(post);
    }

    public void addDelete(int userId, String postId) {
        PostKey postKey = new PostKey(userId, postId);
        deletedKeys.add(postKey);
    }
}
