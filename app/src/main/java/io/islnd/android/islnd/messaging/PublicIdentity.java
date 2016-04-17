package io.islnd.android.islnd.messaging;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.Serializable;
import java.security.Key;
import java.security.PublicKey;

import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.proto.IslandProto;

public class PublicIdentity implements Serializable, ProtoSerializable<PublicIdentity> {
    private final String messageInbox;
    private final PublicKey publicKey;

    public PublicIdentity(
            String messageInbox,
            PublicKey publicKey) {
        this.messageInbox = messageInbox;
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getMessageInbox() {
        return messageInbox;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PublicIdentity)) {
            return false;
        }

        PublicIdentity otherPublicIdentity = (PublicIdentity)other;
        return this.publicKey.equals(otherPublicIdentity.publicKey)
                && this.messageInbox.equals(otherPublicIdentity.messageInbox);
    }

    public static PublicIdentity fromProto(String blob) {
        IslandProto.PublicIdentity identity = null;
        byte[] bytes = new Decoder().decode(blob);
        try {
            identity = IslandProto.PublicIdentity.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            // TODO: Might want to handle for things like sms
            e.printStackTrace();
        }

        return new PublicIdentity(
                identity.getMessageInbox(),
                CryptoUtil.decodePublicKey(identity.getPublicKey()));
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.PublicIdentity.newBuilder()
                .setPublicKey(CryptoUtil.encodeKey(this.getPublicKey()))
                .setMessageInbox(this.getMessageInbox())
                .build()
                .toByteArray();
    }
}
