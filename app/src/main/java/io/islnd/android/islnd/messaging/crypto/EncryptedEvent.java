package io.islnd.android.islnd.messaging.crypto;

import java.security.Key;

import io.islnd.android.islnd.messaging.event.ChangeDisplayNameEvent;
import io.islnd.android.islnd.messaging.event.Event;

public class EncryptedEvent extends SymmetricEncryptedData {

    private final String alias;

    public EncryptedEvent(Event event, Key privateKey, Key groupKey) {
        super(event, privateKey, groupKey);
        this.alias = event.getAlias();
    }

    @Override
    public Event decryptAndVerify(Key groupKey, Key authorPublicKey) throws InvalidSignatureException {
        SignedObject signedObject = this.getSignedAndVerifiedObject(groupKey, authorPublicKey);
        return Event.fromProto(signedObject.getObject());
    }

    public String getAlias() {
        return alias;
    }
}
