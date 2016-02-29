package org.island.messaging;

import com.google.protobuf.InvalidProtocolBufferException;

import org.island.messaging.proto.IslandProto;

public class CommentUpdate implements ProtoSerializable<CommentUpdate> {
    private final boolean isDelete;
    private final String postAuthorPseudonym;
    private final String commentAuthorPseudonym;
    private final String postId;
    private final String commentId;
    private final String content;
    private final long timestamp;

    private CommentUpdate(boolean isDelete, String postAuthorPseudonym, String commentAuthorPseudonym, String postId, String commentId, String content, long timestamp) {
        this.isDelete = isDelete;
        this.postAuthorPseudonym = postAuthorPseudonym;
        this.commentAuthorPseudonym = commentAuthorPseudonym;
        this.postId = postId;
        this.commentId = commentId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public static CommentUpdate buildComment(
            String postAuthorPseudonym,
            String commentAuthorPseudonym,
            String postId,
            String commentId,
            String content) {
        return new CommentUpdate(
                false,
                postAuthorPseudonym,
                commentAuthorPseudonym,
                postId,
                commentId,
                content,
                Util.getContentTimestamp());
    }

    public static CommentUpdate buildDelete(
            String postAuthorPseudonym,
            String commentAuthorPseudonym,
            String postId,
            String commentId) {
        return new CommentUpdate(
                true,
                postAuthorPseudonym,
                commentAuthorPseudonym,
                postId,
                commentId,
                "",
                Util.getContentTimestamp());
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
                .setPostAuthorPseudonym(this.postAuthorPseudonym)
                .setCommentAuthorPseudonym(this.commentAuthorPseudonym)
                .setPostId(this.postId)
                .setCommentId(this.commentId)
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

        return new CommentUpdate(
                commentUpdate.getIsDelete(),
                commentUpdate.getPostAuthorPseudonym(),
                commentUpdate.getCommentAuthorPseudonym(),
                commentUpdate.getPostId(),
                commentUpdate.getCommentId(),
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
                && otherComment.commentId.equals(this.commentId)
                && otherComment.timestamp == this.timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public String getPostAuthorPseudonym() {
        return postAuthorPseudonym;
    }

    public String getCommentAuthorPseudonym() {
        return commentAuthorPseudonym;
    }

    public String getCommentId() {
        return commentId;
    }
}
