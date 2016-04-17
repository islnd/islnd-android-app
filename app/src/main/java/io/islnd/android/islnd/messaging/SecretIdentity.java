package io.islnd.android.islnd.messaging;

import com.google.protobuf.InvalidProtocolBufferException;

import java.security.Key;

import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.proto.IslandProto;

public class SecretIdentity implements ProtoSerializable {
    private final String displayName;
    private final String alias;
    private final Key groupKey;

    public SecretIdentity(String displayName, String alias, Key groupKey) {
        this.displayName = displayName;
        this.alias = alias;
        this.groupKey = groupKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAlias() {
        return alias;
    }

    public Key getGroupKey() {
        return groupKey;
    }

    public static SecretIdentity fromProto(String blob) {
        IslandProto.SecretIdentity secretIdentity = null;
        byte[] bytes = new Decoder().decode(blob);
        try {
            secretIdentity = IslandProto.SecretIdentity.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            // TODO: Might want to handle for things like sms
            e.printStackTrace();
        }

        return new SecretIdentity(
                secretIdentity.getDisplayName(),
                secretIdentity.getAlias(),
                CryptoUtil.decodeSymmetricKey(secretIdentity.getGroupKey()));
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.SecretIdentity.newBuilder()
                .setGroupKey(CryptoUtil.encodeKey(this.getGroupKey()))
                .setAlias(this.getAlias())
                .setDisplayName(this.getDisplayName())
                .build()
                .toByteArray();
    }
}
