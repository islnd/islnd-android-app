package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import io.islnd.android.islnd.messaging.ProfileResource;
import io.islnd.android.islnd.messaging.ProtoSerializable;

public class EncryptedResource extends SymmetricEncryptedData {

    private final String resourceKey;

    public EncryptedResource(ProtoSerializable object, PrivateKey privateKey, Key groupKey, String resourceKey) {
        super(object, privateKey, groupKey);

        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    @Override
    public ProfileResource decryptAndVerify(SecretKey groupKey, PublicKey authorPublicKey) throws InvalidSignatureException {
        return ProfileResource.fromProto(verifySignatureAndGetObject(groupKey, authorPublicKey));
    }
}
