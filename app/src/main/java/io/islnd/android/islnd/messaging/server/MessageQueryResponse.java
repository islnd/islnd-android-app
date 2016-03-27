package io.islnd.android.islnd.messaging.server;

import java.util.List;

import io.islnd.android.islnd.messaging.message.Message;

public class MessageQueryResponse {
    private Data data;

    private class Data {
        List<Message> messages;
    }

    public List<Message> getMessages() {
        return this.data.messages;
    }
}
