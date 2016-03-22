package io.islnd.android.islnd.messaging.event;

import io.islnd.android.islnd.messaging.proto.IslandProto;

public class DeleteCommentEvent extends Event {

    private final String commentId;

    protected DeleteCommentEvent(String alias, int eventId, String commentId) {
        super(alias, eventId, EventType.DELETE_COMMENT);

        this.commentId = commentId;
    }

    public String getCommentId() {
        return commentId;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Event.newBuilder()
                .setAlias(this.alias)
                .setEventId(this.eventId)
                .setEventType(this.eventType)
                .setContentId(this.commentId)
                .build()
                .toByteArray();
    }
}
