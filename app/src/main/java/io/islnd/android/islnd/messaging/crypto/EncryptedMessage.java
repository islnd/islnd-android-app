package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;

import io.islnd.android.islnd.messaging.ProtoSerializable;
import io.islnd.android.islnd.messaging.message.Message;

public class EncryptedMessage extends AsymmetricEncryptedData {

    private final String mailbox;

    public EncryptedMessage(Message message, Key publicKey) {
        super(message, publicKey);
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

    @Override
    public Message decrypt(Key privateKey) {
        byte[] decryptedBytes = ObjectEncrypter.decryptAsymmetric(this.blob, privateKey);
        return Message.fromProto(decryptedBytes);
    }
}
