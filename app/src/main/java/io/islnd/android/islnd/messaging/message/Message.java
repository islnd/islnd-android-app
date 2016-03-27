package io.islnd.android.islnd.messaging.message;

public class Message implements Comparable<Message> {

    private final String mailbox;
    private final int type;
    private final String blob;

    public Message(String mailbox, int type, String blob) {
        this.mailbox = mailbox;
        this.type = type;
        this.blob = blob;
    }

    public String getMailbox() {
        return mailbox;
    }

    public int getType() {
        return type;
    }

    public String getBlob() {
        return blob;
    }


    @Override
    public int compareTo(Message another) {
        return type - another.type;
    }
}
