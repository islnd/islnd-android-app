package io.islnd.android.islnd.messaging.event;

import io.islnd.android.islnd.messaging.proto.IslandProto;

public class NewPostEvent extends Event {

    private final String postId;
    private final String content;
    private final long timestamp;

    protected NewPostEvent(String alias, int eventId, String postId, String content, long timestamp) {
        super(alias, eventId, EventType.NEW_POST);

        this.postId = postId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Event.newBuilder()
                .setAlias(this.alias)
                .setEventId(this.eventId)
                .setEventType(this.eventType)
                .setContentId(this.postId)
                .setTextContent(this.content)
                .setTimestamp(this.timestamp)
                .build()
                .toByteArray();
    }
}
