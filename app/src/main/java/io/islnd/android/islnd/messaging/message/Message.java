package io.islnd.android.islnd.messaging.message;

import com.google.protobuf.InvalidProtocolBufferException;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.ProtoSerializable;
import io.islnd.android.islnd.messaging.proto.IslandProto;

public class Message implements Comparable<Message>, ProtoSerializable {

    private final String mailbox;
    private final String nonce;
    private final int messageId;
    private final int type;
    private final String blob;

    public Message(String mailbox, String nonce, int messageId, int type, String blob) {
        this.mailbox = mailbox;
        this.nonce = nonce;
        this.messageId = messageId;
        this.type = type;
        this.blob = blob;
    }

    public Message(String mailbox, int messageId, int type, String blob) {
        this.mailbox = mailbox;
        this.nonce = null;
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

    public String getNonce() {
        return nonce;
    }

    @Override
    public int compareTo(Message another) {
        return type - another.type;
    }

    @Override
    public byte[] toByteArray() {
        IslandProto.Message.Builder builder = IslandProto.Message.newBuilder()
                .setMailbox(this.mailbox)
                .setMessageId(this.messageId)
                .setType(this.type)
                .setBlob(this.blob);

        if (this.nonce != null) {
            builder.setNonce(this.nonce);
        }

        return builder.build()
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

        if (message.hasNonce()) {
            return new Message(
                    message.getMailbox(),
                    message.getNonce(),
                    message.getMessageId(),
                    message.getType(),
                    message.getBlob()
            );
        } else {
            return new Message(
                    message.getMailbox(),
                    message.getMessageId(),
                    message.getType(),
                    message.getBlob()
            );
        }
    }

    @Override
    public String toString() {
        return "MESSAGE: " + mailbox + " " + messageId;
    }
}
