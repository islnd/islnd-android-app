package io.islnd.android.islnd.messaging.message;

public class Message {

    private final String mailbox;
    private final String blob;

    public Message(String mailbox, String blob) {
        this.mailbox = mailbox;
        this.blob = blob;
    }

    public String getBlob() {
        return blob;
    }
}
