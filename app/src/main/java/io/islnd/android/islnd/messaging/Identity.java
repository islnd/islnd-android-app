package io.islnd.android.islnd.messaging;

import com.google.protobuf.InvalidProtocolBufferException;

import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.proto.IslandProto;

import java.io.Serializable;
import java.security.Key;

public class Identity implements Serializable, ProtoSerializable<Identity> {
    private final String displayName;
    private final String alias;
    private final Key groupKey;
    private final Key publicKey;

    public String getAlias() {
        return alias;
    }

    public Key getGroupKey() {
        return groupKey;
    }

    public Key getPublicKey() {
        return publicKey;
    }

    public Identity(String displayName, String alias, Key groupKey, Key publicKey) {
        this.displayName = displayName;
        this.alias = alias;
        this.groupKey = groupKey;
        this.publicKey = publicKey;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Identity)) {
            return false;
        }

        Identity otherKey = (Identity)other;
        return this.alias.equals(otherKey.alias)
                && this.displayName.equals(otherKey.displayName)
                && this.groupKey.equals(otherKey.groupKey)
                && this.publicKey.equals(otherKey.publicKey);
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static Identity fromProto(byte[] bytes) {
        IslandProto.Identity identity = null;
        try {
            identity = IslandProto.Identity.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            // TODO: Might want to handle for things like sms
            e.printStackTrace();
        }

        return new Identity(
                identity.getDisplayName(),
                identity.getAlias(),
                CryptoUtil.decodeSymmetricKey(identity.getGroupKey()),
                CryptoUtil.decodePublicKey(identity.getPublicKey()));
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Identity.newBuilder()
                .setGroupKey(CryptoUtil.encodeKey(this.getGroupKey()))
                .setPublicKey(CryptoUtil.encodeKey(this.getPublicKey()))
                .setAlias(this.getAlias())
                .setDisplayName(this.getDisplayName())
                .build()
                .toByteArray();
    }
}
