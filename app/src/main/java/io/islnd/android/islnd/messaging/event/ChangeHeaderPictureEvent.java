package io.islnd.android.islnd.messaging.event;

import com.google.protobuf.ByteString;

import io.islnd.android.islnd.messaging.proto.IslandProto;

public class ChangeHeaderPictureEvent extends Event {

    private final byte[] headerPicture;

    protected ChangeHeaderPictureEvent(String alias, int eventId, byte[] headerPicture) {
        super(alias, eventId, EventType.CHANGE_HEADER_PICTURE);
        this.headerPicture = headerPicture;
    }

    public byte[] getHeaderPicture() {
        return headerPicture;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Event.newBuilder()
                .setAlias(this.alias)
                .setEventId(this.eventId)
                .setEventType(this.eventType)
                .setDataContent(ByteString.copyFrom(this.headerPicture))
                .build()
                .toByteArray();
    }
}
