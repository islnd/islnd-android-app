package io.islnd.android.islnd.messaging.crypto;

import com.google.protobuf.InvalidProtocolBufferException;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import io.islnd.android.islnd.messaging.InvalidBlobException;
import io.islnd.android.islnd.messaging.message.Message;
import io.islnd.android.islnd.messaging.message.ReceivedMessage;

public class EncryptedMessage extends AsymmetricEncryptedData {

    private final String mailbox;

    public EncryptedMessage(Message message, Key recipientPublicKey, PrivateKey authorPrivateKey) {
        super(message, recipientPublicKey, authorPrivateKey);
        this.mailbox = message.getMailbox();
    }

    public EncryptedMessage(String mailbox, String blob) {
        super(blob);
        this.mailbox = mailbox;
        this.blob = blob;
    }

    public String getMailbox() {
        return mailbox;
    }

    public ReceivedMessage decryptMessageAndCheckSignature(Key privateKey, PublicKey authorPublicKey) throws InvalidProtocolBufferException, InvalidBlobException {
        SignedObject signedObject = SignedObject.fromProto(
                ObjectEncrypter.decryptAsymmetric(
                        this.blob,
                        privateKey));

        if (authorPublicKey == null) {
            return new ReceivedMessage(Message.fromProto(signedObject.getObject()));
        }

        return new ReceivedMessage(
                Message.fromProto(signedObject.getObject()),
                CryptoUtil.verifySignedObject(signedObject, authorPublicKey));
    }
}
