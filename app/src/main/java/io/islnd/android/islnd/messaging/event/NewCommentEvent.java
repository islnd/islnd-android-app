package io.islnd.android.islnd.messaging.event;

import io.islnd.android.islnd.messaging.proto.IslandProto;

public class NewCommentEvent extends Event {

    private final String postId;
    private final String postAuthorAlias;
    private final String commentId;
    private final String content;
    private final long timestamp;

    protected NewCommentEvent(
            String alias,
            int eventId,
            String postId,
            String postAuthorAlias,
            String commentId,
            String content,
            long timestamp) {
        super(alias, eventId, EventType.NEW_COMMENT);

        this.postId = postId;
        this.postAuthorAlias = postAuthorAlias;
        this.commentId = commentId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getPostId() {
        return this.postId;
    }

    public String getPostAuthorAlias() {
        return this.postAuthorAlias;
    }

    public String getCommentId() {
        return this.commentId;
    }

    public String getContent() {
        return this.content;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Event.newBuilder()
                .setAlias(this.alias)
                .setEventId(this.eventId)
                .setEventType(this.eventType)
                .setContentId(this.commentId)
                .setParentAlias(this.postAuthorAlias)
                .setParentContentId(this.postId)
                .setTextContent(this.content)
                .setTimestamp(this.timestamp)
                .build()
                .toByteArray();
    }
}
