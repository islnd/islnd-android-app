package io.islnd.android.islnd.messaging.server;

import io.islnd.android.islnd.messaging.crypto.EncryptedComment;

import java.util.List;

public class CommentQueryResponse {
    private Data data;

    private class Data {
        List<EncryptedComment> comments;
    }

    public List<EncryptedComment> getComments() {
        return this.data.comments;
    }
}
