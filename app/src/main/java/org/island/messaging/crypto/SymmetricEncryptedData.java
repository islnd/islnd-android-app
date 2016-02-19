package org.island.messaging.crypto;

import org.island.messaging.ProtoSerializable;

import java.security.Key;

public abstract class SymmetricEncryptedData<T extends ProtoSerializable> extends EncryptedData {
    public SymmetricEncryptedData(T object, Key privateKey, Key groupKey) {
        super();
        SignedObject signedObject = ObjectSigner.sign(object, privateKey);
        blob = ObjectEncrypter.encryptSymmetric(signedObject, groupKey);
    }

    public abstract T decrypt(Key groupKey);

    protected SignedObject getSignedObject(Key groupKey) {
        return SignedObject.fromProto(ObjectEncrypter.decryptSymmetric(this.getBlob(), groupKey));
    }
}
