package io.islnd.android.islnd.messaging.event;

import io.islnd.android.islnd.messaging.proto.IslandProto;

public class ChangeAliasEvent extends Event {

    private final String newAlias;

    protected ChangeAliasEvent(String alias, int eventId, String newAlias) {
        super(alias, eventId, EventType.CHANGE_ALIAS);

        this.newAlias = newAlias;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Event.newBuilder()
                .setAlias(this.alias)
                .setEventId(this.eventId)
                .setEventType(this.eventType)
                .setTextContent(this.newAlias)
                .build()
                .toByteArray();
    }

    public String getNewAlias() {
        return newAlias;
    }
}
