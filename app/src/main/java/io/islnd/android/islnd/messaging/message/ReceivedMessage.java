package io.islnd.android.islnd.messaging.message;

public class ReceivedMessage {
    private final Message message;
    private final boolean signatureValid;

    public ReceivedMessage(Message message) {
        this.message = message;
        this.signatureValid = false;
    }

    public ReceivedMessage(Message message, boolean signatureValid) {
        this.message = message;
        this.signatureValid = signatureValid;
    }

    public Message getMessage() {
        return message;
    }

    public boolean isSignatureValid() {
        return signatureValid;
    }
}
