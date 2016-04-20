package io.islnd.android.islnd.messaging;

import java.security.PublicKey;

public class PublicKeyAndInbox {
    private final PublicKey publicKey;
    private final String inbox;

    public PublicKeyAndInbox(PublicKey publicKey, String inbox) {
        this.publicKey = publicKey;
        this.inbox = inbox;
    }

    public String getInbox() {
        return inbox;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
