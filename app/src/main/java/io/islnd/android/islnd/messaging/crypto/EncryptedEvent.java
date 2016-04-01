package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import io.islnd.android.islnd.messaging.event.Event;

public class EncryptedEvent extends SymmetricEncryptedData {

    private final String alias;

    public EncryptedEvent(Event event, PrivateKey privateKey, Key groupKey) {
        super(event, privateKey, groupKey);
        this.alias = event.getAlias();
    }

    public EncryptedEvent(String blob, String alias) {
        super(blob);
        this.alias = alias;
    }

    @Override
    public Event decryptAndVerify(Key groupKey, PublicKey authorPublicKey) throws InvalidSignatureException {
        String object = this.verifySignatureAndGetObject(groupKey, authorPublicKey);
        return Event.fromProto(object);
    }

    public String getAlias() {
        return alias;
    }
}
