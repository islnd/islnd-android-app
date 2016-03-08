package io.islnd.android.islnd.messaging.crypto;

import io.islnd.android.islnd.messaging.PostUpdate;

import java.security.Key;

public class EncryptedPost extends SymmetricEncryptedData {
    public EncryptedPost(PostUpdate post, Key privateKey, Key groupKey) {
        super(post, privateKey, groupKey);
    }

    @Override
    public PostUpdate decryptAndVerify(Key groupKey, Key authorPublicKey) throws InvalidSignatureException {
        SignedObject signedObject = this.getSignedAndVerifiedObject(groupKey, authorPublicKey);
        return PostUpdate.fromProto(signedObject.getObject());
    }
}