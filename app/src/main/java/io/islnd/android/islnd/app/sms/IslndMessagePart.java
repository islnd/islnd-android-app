package io.islnd.android.islnd.app.sms;

public class IslndMessagePart {
    private final String originatingAddress;
    private final String messageId;
    private final int messagePartId;
    private final int lastMessagePartId;
    private final String body;

    public IslndMessagePart(String originatingAddress, String messageId, int messagePartId, int lastMessagePartId, String body) {
        this.originatingAddress = originatingAddress;
        this.messageId = messageId;
        this.messagePartId = messagePartId;
        this.lastMessagePartId = lastMessagePartId;
        this.body = body;
    }

    public String getOriginatingAddress() {
        return originatingAddress;
    }

    public String getMessageId() {
        return messageId;
    }

    public int getMessagePartId() {
        return messagePartId;
    }

    public int getLastMessagePartId() {
        return lastMessagePartId;
    }

    public String getBody() {
        return body;
    }
}
