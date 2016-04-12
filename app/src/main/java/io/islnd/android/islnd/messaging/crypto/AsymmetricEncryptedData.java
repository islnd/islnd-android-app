package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;

import io.islnd.android.islnd.messaging.ProtoSerializable;

public abstract class AsymmetricEncryptedData<T extends ProtoSerializable> extends EncryptedData {
    public AsymmetricEncryptedData(T object, Key publicKey) {
        blob = ObjectEncrypter.encryptAsymmetric(object, publicKey);
    }

    public AsymmetricEncryptedData(String blob) {
        this.blob = blob;
    }

    public abstract T decrypt(Key privateKey);
}
