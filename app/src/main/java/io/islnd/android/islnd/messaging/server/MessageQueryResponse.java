package io.islnd.android.islnd.messaging.server;

import java.util.List;

import io.islnd.android.islnd.messaging.crypto.EncryptedMessage;

public class MessageQueryResponse {
    private Data data;

    private class Data {
        List<EncryptedMessage> messages;
    }

    public List<EncryptedMessage> getMessages() {
        return this.data.messages;
    }
}
