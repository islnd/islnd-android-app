package org.island.messaging;

import com.google.protobuf.InvalidProtocolBufferException;

import org.island.messaging.proto.IslandProto;

public class PostUpdate implements ProtoSerializable<PostUpdate> {
    private final boolean isDelete;
    private final String id;
    private final String content;
    private final long timestamp;

    private PostUpdate(String id, String content, boolean isDelete) {
        this(id, content, isDelete, System.currentTimeMillis());
    }

    private PostUpdate(String id, String content, boolean isDelete, long timestamp) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.isDelete = isDelete;
    }

    public static PostUpdate buildPost(String content, String id) {
        return new PostUpdate(id, content, false);
    }

    public static PostUpdate buildDelete(String postId) {
        return new PostUpdate(postId, "", true);
    }

    public String getId() {
        return id;
    }

    public boolean isDeletion() {
        return isDelete;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.PostUpdate.newBuilder()
                .setContent(this.content)
                .setId(this.id)
                .setIsDelete(this.isDelete)
                .setTimestamp(this.timestamp)
                .build()
                .toByteArray();
    }

    public static PostUpdate fromProto(String string) {
        return fromProto(new Decoder().decode(string));
    }

    public static PostUpdate fromProto(byte[] bytes) {
        IslandProto.PostUpdate postUpdate = null;
        try {
            postUpdate = IslandProto.PostUpdate.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return new PostUpdate(postUpdate.getId(),
                postUpdate.getContent(),
                postUpdate.getIsDelete(),
                postUpdate.getTimestamp());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PostUpdate)) {
            return false;
        }

        PostUpdate otherPost = (PostUpdate)other;
        return otherPost.content.equals(this.content)
                && otherPost.isDelete == this.isDelete
                && otherPost.id.equals(this.id)
                && otherPost.timestamp == this.timestamp;
    }
}