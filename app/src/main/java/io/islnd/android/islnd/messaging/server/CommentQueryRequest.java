package io.islnd.android.islnd.messaging.server;

import java.util.List;

public class CommentQueryRequest {
    private final List<CommentQuery> data;

    public CommentQueryRequest(List<CommentQuery> data) {
        this.data = data;
    }
}
