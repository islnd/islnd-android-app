package org.island.messaging.crypto;

import org.island.messaging.PostUpdate;
import org.island.messaging.ProtoSerializable;

import java.security.Key;

public class EncryptedPost extends SymmetricEncryptedData {
    public EncryptedPost(PostUpdate post, Key privateKey, Key groupKey) {
        super(post, privateKey, groupKey);
    }

    @Override
    public PostUpdate decrypt(Key groupKey) {
        SignedObject signedObject = this.getSignedObject(groupKey);
        return PostUpdate.fromProto(signedObject.getObject());
    }
}
