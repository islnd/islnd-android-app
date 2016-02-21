package org.island.messaging;

import com.google.protobuf.InvalidProtocolBufferException;

import org.island.messaging.proto.IslandProto;

public class CommentUpdate implements ProtoSerializable<CommentUpdate> {
    private final boolean isDelete;
    private final String commentAuthorPseudonym;
    private final String postAuthorPseudonym;
    private final String postId;
    private final String content;
    private final long timestamp;

    private CommentUpdate(boolean isDelete, String commentAuthorPseudonym, String postAuthorPseudonym, String postId, String content, long timestamp) {
        this.isDelete = isDelete;
        this.commentAuthorPseudonym = commentAuthorPseudonym;
        this.postAuthorPseudonym = postAuthorPseudonym;
        this.postId = postId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public static CommentUpdate buildComment(String commentAuthorPseudonym, String postAuthorPseudonym, String postId, String content) {
        return new CommentUpdate(false, commentAuthorPseudonym, postAuthorPseudonym, postId, content, Util.getContentTimestamp());
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
        return IslandProto.CommentUpdate.newBuilder()
                .setContent(this.content)
                .setPostAuthor(this.postAuthorPseudonym)
                .setPostId(this.postId)
                .setIsDelete(this.isDelete)
                .setTimestamp(this.timestamp)
                .build()
                .toByteArray();
    }

    public static CommentUpdate fromProto(String string) {
        return fromProto(new Decoder().decode(string));
    }

    public static CommentUpdate fromProto(byte[] bytes) {
        IslandProto.CommentUpdate commentUpdate = null;
        try {
            commentUpdate = IslandProto.CommentUpdate.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return new CommentUpdate(commentUpdate.getIsDelete(),
                commentUpdate.getPostAuthor(),
                commentUpdate.getPostId(),
                commentUpdate.getContent(),
                commentUpdate.getTimestamp());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CommentUpdate)) {
            return false;
        }

        CommentUpdate otherComment = (CommentUpdate)other;
        return otherComment.content.equals(this.content)
                && otherComment.isDelete == this.isDelete
                && otherComment.postAuthorPseudonym.equals(this.postAuthorPseudonym)
                && otherComment.postId.equals(this.postId)
                && otherComment.timestamp == this.timestamp;
    }

    public String getPostId() {
        return postId;
    }
}
