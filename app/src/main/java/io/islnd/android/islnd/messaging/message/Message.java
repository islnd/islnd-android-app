package io.islnd.android.islnd.messaging.message;

import com.google.protobuf.InvalidProtocolBufferException;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.ProtoSerializable;
import io.islnd.android.islnd.messaging.proto.IslandProto;

public class Message implements Comparable<Message>, ProtoSerializable {

    private final String mailbox;
    private final int messageId;
    private final int type;
    private final String blob;

    public Message(String mailbox, int messageId, int type, String blob) {
        this.mailbox = mailbox;
        this.messageId = messageId;
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

    public int getMessageId() {
        return messageId;
    }

    @Override
    public int compareTo(Message another) {
        return type - another.type;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Message.newBuilder()
                .setMailbox(this.mailbox)
                .setMessageId(this.messageId)
                .setType(this.type)
                .setBlob(this.blob)
                .build()
                .toByteArray();
    }

    public static Message fromProto(String encodedObject) {
        byte[] bytes = new Decoder().decode(encodedObject);
        IslandProto.Message message = null;
        try {
            message = IslandProto.Message.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return new Message(
                message.getMailbox(),
                message.getMessageId(),
                message.getType(),
                message.getBlob()
        );
    }

    @Override
    public String toString() {
        return "MESSAGE: " + mailbox + " " + messageId;
    }
}
