package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;
import java.security.PrivateKey;

import io.islnd.android.islnd.messaging.ProtoSerializable;

public abstract class AsymmetricEncryptedData extends EncryptedData {
    public AsymmetricEncryptedData(ProtoSerializable object, Key recipientPublicKey, PrivateKey authorPrivateKey) {
        SignedObject signedObject = CryptoUtil.sign(object, authorPrivateKey);
        blob = ObjectEncrypter.encryptAsymmetric(signedObject, recipientPublicKey);
    }

    public AsymmetricEncryptedData(String blob) {
        this.blob = blob;
    }
}
