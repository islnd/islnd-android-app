package io.islnd.android.islnd.messaging.event;

import com.google.protobuf.InvalidProtocolBufferException;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.proto.IslandProto;

public class DeletePostEvent extends Event {

    private final String postId;

    protected DeletePostEvent(String alias, int eventId, String postId) {
        super(alias, eventId, EventType.DELETE_POST);

        this.postId = postId;
    }

    public String getPostId() {
        return postId;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Event.newBuilder()
                .setAlias(this.alias)
                .setEventId(this.eventId)
                .setEventType(this.eventType)
                .setContentId(this.postId)
                .build()
                .toByteArray();
    }
}
