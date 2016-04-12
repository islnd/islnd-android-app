package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import io.islnd.android.islnd.messaging.ProtoSerializable;

public abstract class SymmetricEncryptedData<T extends ProtoSerializable> extends EncryptedData {
    private static final String TAG = SymmetricEncryptedData.class.getSimpleName();

    public SymmetricEncryptedData(T object, Key privateKey, Key groupKey) {
        SignedObject signedObject = ObjectSigner.sign(object, privateKey);
        blob = ObjectEncrypter.encryptSymmetric(signedObject, groupKey);
    }

    public SymmetricEncryptedData(String blob) {
        this.blob = blob;
    }

    public abstract T decryptAndVerify(SecretKey groupKey, PublicKey authorPublicKey) throws InvalidSignatureException;

    protected SignedObject getSignedAndVerifiedObject(SecretKey groupKey, PublicKey authorPublicKey) throws InvalidSignatureException {
        SignedObject signedObject = SignedObject.fromProto(ObjectEncrypter.decryptSymmetric(this.getBlob(), groupKey));
        if (!ObjectSigner.verify(signedObject, authorPublicKey)) {
            throw new InvalidSignatureException();
        }

        return signedObject;
    }
}
