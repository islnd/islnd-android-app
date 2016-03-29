package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;

import io.islnd.android.islnd.messaging.message.Message;

public class EncryptedMessage extends AsymmetricEncryptedData {

    private final String mailbox;

    public EncryptedMessage(Message message, Key publicKey) {
        super(message, publicKey);
        this.mailbox = message.getMailbox();
    }

    @Override
    public Message decrypt(Key privateKey) {
        byte[] decryptedBytes = ObjectEncrypter.decryptAsymmetric(this.blob, privateKey);
        return Message.fromProto(decryptedBytes);
    }
}