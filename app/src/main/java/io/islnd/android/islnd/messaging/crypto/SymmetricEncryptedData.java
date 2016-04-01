package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import io.islnd.android.islnd.messaging.ProtoSerializable;

public abstract class SymmetricEncryptedData<T extends ProtoSerializable> extends EncryptedData {
    private static final String TAG = SymmetricEncryptedData.class.getSimpleName();

    public SymmetricEncryptedData(T object, PrivateKey privateKey, Key groupKey) {
        SignedObject signedObject = CryptoUtil.sign(object, privateKey);
        blob = ObjectEncrypter.encryptSymmetric(signedObject, groupKey);
    }

    public SymmetricEncryptedData(String blob) {
        this.blob = blob;
    }

    public abstract T decryptAndVerify(Key groupKey, PublicKey authorPublicKey) throws InvalidSignatureException;

    protected SignedObject getSignedObject(Key groupKey) {
        return SignedObject.fromProto(ObjectEncrypter.decryptSymmetric(this.getBlob(), groupKey));
    }

    protected SignedObject getSignedAndVerifiedObject(Key groupKey, PublicKey authorPublicKey) throws InvalidSignatureException {
        SignedObject signedObject = SignedObject.fromProto(ObjectEncrypter.decryptSymmetric(this.getBlob(), groupKey));
        if (!CryptoUtil.verifySignedObject(signedObject, authorPublicKey)) {
            throw new InvalidSignatureException();
        }

        return signedObject;
    }
}
