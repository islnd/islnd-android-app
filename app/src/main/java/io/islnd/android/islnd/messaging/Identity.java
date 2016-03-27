package io.islnd.android.islnd.messaging;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.Serializable;
import java.security.Key;

import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.proto.IslandProto;

public class Identity implements Serializable, ProtoSerializable<Identity> {
    private final String displayName;
    private final String alias;
    private final String messageInbox;
    private final Key groupKey;
    private final Key publicKey;

    public Identity(
            String displayName,
            String alias,
            String messageInbox,
            Key groupKey,
            Key publicKey) {
        this.displayName = displayName;
        this.alias = alias;
        this.messageInbox = messageInbox;
        this.groupKey = groupKey;
        this.publicKey = publicKey;
    }

    public String getAlias() {
        return alias;
    }

    public Key getGroupKey() {
        return groupKey;
    }

    public Key getPublicKey() {
        return publicKey;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getMessageInbox() {
        return messageInbox;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Identity)) {
            return false;
        }

        Identity otherIdentity = (Identity)other;
        return this.alias.equals(otherIdentity.alias)
                && this.displayName.equals(otherIdentity.displayName)
                && this.groupKey.equals(otherIdentity.groupKey)
                && this.publicKey.equals(otherIdentity.publicKey)
                && this.messageInbox.equals(otherIdentity.messageInbox);
    }

    public static Identity fromProto(String blob) {
        IslandProto.Identity identity = null;
        byte[] bytes = new Decoder().decode(blob);
        try {
            identity = IslandProto.Identity.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            // TODO: Might want to handle for things like sms
            e.printStackTrace();
        }

        return new Identity(
                identity.getDisplayName(),
                identity.getAlias(),
                identity.getMessageInbox(),
                CryptoUtil.decodeSymmetricKey(identity.getGroupKey()),
                CryptoUtil.decodePublicKey(identity.getPublicKey()));
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Identity.newBuilder()
                .setGroupKey(CryptoUtil.encodeKey(this.getGroupKey()))
                .setPublicKey(CryptoUtil.encodeKey(this.getPublicKey()))
                .setAlias(this.getAlias())
                .setMessageInbox(this.getMessageInbox())
                .setDisplayName(this.getDisplayName())
                .build()
                .toByteArray();
    }
}
