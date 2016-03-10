package io.islnd.android.islnd.messaging.crypto;

import io.islnd.android.islnd.messaging.ProtoSerializable;

import java.security.Key;

public abstract class SymmetricEncryptedData<T extends ProtoSerializable> extends EncryptedData {
    public SymmetricEncryptedData(T object, Key privateKey, Key groupKey) {
        super();
        SignedObject signedObject = ObjectSigner.sign(object, privateKey);
        blob = ObjectEncrypter.encryptSymmetric(signedObject, groupKey);
    }

    public abstract T decryptAndVerify(Key groupKey, Key authorPublicKey) throws InvalidSignatureException;

    protected SignedObject getSignedObject(Key groupKey) {
        return SignedObject.fromProto(ObjectEncrypter.decryptSymmetric(this.getBlob(), groupKey));
    }

    protected SignedObject getSignedAndVerifiedObject(Key groupKey, Key authorPublicKey) throws InvalidSignatureException {
        SignedObject signedObject = SignedObject.fromProto(ObjectEncrypter.decryptSymmetric(this.getBlob(), groupKey));
        if (!ObjectSigner.verify(signedObject, authorPublicKey)) {
            throw new InvalidSignatureException();
        }

        return signedObject;
    }
}
