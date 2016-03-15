package io.islnd.android.islnd.messaging.server;

import java.util.List;

import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;

public class EventQueryResponse {
    private Data data;

    private class Data {
        List<EncryptedEvent> events;
    }

    public List<EncryptedEvent> getEvents() {
        return this.data.events;
    }
}
