package io.islnd.android.islnd.messaging.event;

import io.islnd.android.islnd.messaging.proto.IslandProto;

public class ChangeDisplayNameEvent extends Event {

    private final String newDisplayName;

    protected ChangeDisplayNameEvent(String alias, int id, String newDisplayName) {
        super(alias, id, EventType.CHANGE_DISPLAY_NAME);
        this.newDisplayName = newDisplayName;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Event.newBuilder()
                .setAlias(this.alias)
                .setEventId(this.eventId)
                .setEventType(this.eventType)
                .setTextContent(this.newDisplayName)
                .build()
                .toByteArray();
    }

    public String getNewDisplayName() {
        return newDisplayName;
    }
}
