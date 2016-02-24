package org.island.messaging.server;

import org.island.messaging.crypto.EncryptedComment;

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
