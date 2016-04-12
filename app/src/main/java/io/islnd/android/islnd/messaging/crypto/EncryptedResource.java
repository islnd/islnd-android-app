package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import io.islnd.android.islnd.messaging.ProfileResource;
import io.islnd.android.islnd.messaging.ProtoSerializable;

public class EncryptedResource extends SymmetricEncryptedData {

    private final String resourceKey;

    public EncryptedResource(ProtoSerializable object, Key privateKey, Key groupKey, String resourceKey) {
        super(object, privateKey, groupKey);

        this.resourceKey = resourceKey;
    }

    @Override
    public ProtoSerializable decryptAndVerify(SecretKey groupKey, PublicKey authorPublicKey) throws InvalidSignatureException {
        SignedObject signedObject = this.getSignedAndVerifiedObject(groupKey, authorPublicKey);
        return ProfileResource.fromProto(signedObject.getObject());
    }

    public String getResourceKey() {
        return resourceKey;
    }
}
