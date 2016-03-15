package io.islnd.android.islnd.messaging.crypto;

import io.islnd.android.islnd.messaging.CommentUpdate;

import java.security.Key;

public class EncryptedComment extends SymmetricEncryptedData {
    private final String postAuthorPseudonym;
    private final String postId;

    public EncryptedComment(CommentUpdate comment, Key privateKey, Key groupKey, String postAuthorPseudonym, String postId) {
        super(comment, privateKey, groupKey);
        this.postAuthorPseudonym = postAuthorPseudonym;
        this.postId = postId;
    }

    @Override
    public CommentUpdate decryptAndVerify(Key groupKey, Key authorPublicKey) throws InvalidSignatureException {
        SignedObject signedObject = this.getSignedAndVerifiedObject(groupKey, authorPublicKey);
        return CommentUpdate.fromProto(signedObject.getObject());
    }

    public String getPostAuthorPseudonym() {
        return postAuthorPseudonym;
    }

    public CommentUpdate decrypt(Key groupKey) {
        SignedObject signedObject = this.getSignedObject(groupKey);
        return CommentUpdate.fromProto(signedObject.getObject());
    }
}
