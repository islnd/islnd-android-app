package io.islnd.android.islnd.messaging.crypto;

import com.google.protobuf.InvalidProtocolBufferException;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import io.islnd.android.islnd.messaging.InvalidBlobException;
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

    public abstract ProtoSerializable decryptAndVerify(SecretKey groupKey, PublicKey authorPublicKey) throws InvalidSignatureException, InvalidBlobException, InvalidProtocolBufferException;

    protected final String verifySignatureAndGetObject(SecretKey groupKey, PublicKey authorPublicKey) throws InvalidSignatureException, InvalidBlobException, InvalidProtocolBufferException {
        final byte[] decryptedBytes = ObjectEncrypter.decryptSymmetric(this.getBlob(), groupKey);
        if (decryptedBytes == null) {
            throw new InvalidBlobException("could not decrypt blob");
        }

        SignedObject signedObject = SignedObject.fromProto(decryptedBytes);
        if (!CryptoUtil.verifySignedObject(signedObject, authorPublicKey)) {
            throw new InvalidSignatureException();
        }

        return signedObject.getObject();
    }
}
