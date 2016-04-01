package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import io.islnd.android.islnd.messaging.ProfileResource;
import io.islnd.android.islnd.messaging.ProtoSerializable;

public class EncryptedResource extends SymmetricEncryptedData {

    private final String resourceKey;

    public EncryptedResource(ProtoSerializable object, PrivateKey privateKey, Key groupKey, String resourceKey) {
        super(object, privateKey, groupKey);

        this.resourceKey = resourceKey;
    }

    @Override
    public ProtoSerializable decryptAndVerify(Key groupKey, PublicKey authorPublicKey) throws InvalidSignatureException {
        String object = this.verifySignatureAndGetObject(groupKey, authorPublicKey);
        return ProfileResource.fromProto(object);
    }

    public String getResourceKey() {
        return resourceKey;
    }
}
