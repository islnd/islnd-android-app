package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import io.islnd.android.islnd.messaging.ProtoSerializable;

public abstract class SymmetricEncryptedData extends EncryptedData {
    private static final String TAG = SymmetricEncryptedData.class.getSimpleName();

    public SymmetricEncryptedData(ProtoSerializable object, PrivateKey privateKey, Key groupKey) {
        SignedObject signedObject = CryptoUtil.sign(object, privateKey);
        blob = ObjectEncrypter.encryptSymmetric(signedObject, groupKey);
    }

    public SymmetricEncryptedData(String blob) {
        this.blob = blob;
    }

    public abstract ProtoSerializable decryptAndVerify(Key groupKey, PublicKey authorPublicKey) throws InvalidSignatureException;

    protected String verifySignatureAndGetObject(Key groupKey, PublicKey authorPublicKey) throws InvalidSignatureException {
        SignedObject signedObject = SignedObject.fromProto(ObjectEncrypter.decryptSymmetric(this.getBlob(), groupKey));
        if (!CryptoUtil.verifySignedObject(signedObject, authorPublicKey)) {
            throw new InvalidSignatureException();
        }

        return signedObject.getObject();
    }
}
