package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import io.islnd.android.islnd.messaging.ProtoSerializable;
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

    public String getAlias() {
        return alias;
    }

    @Override
    public Event decryptAndVerify(SecretKey groupKey, PublicKey authorPublicKey) throws InvalidSignatureException {
        return Event.fromProto(verifySignatureAndGetObject(groupKey, authorPublicKey));
    }
}
