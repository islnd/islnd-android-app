package io.islnd.android.islnd.messaging.message;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.ProtoSerializable;
import io.islnd.android.islnd.messaging.proto.IslandProto;

public class ProfileMessage implements ProtoSerializable<ProfileMessage> {
    private final String resourceKey;

    public ProfileMessage(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return this.resourceKey;
    }

    public static ProfileMessage fromProto(String encodedBytes) {
        IslandProto.ProfileMessage profileMessage = null;
        byte[] bytes = new Decoder().decode(encodedBytes);
        try {
            profileMessage = IslandProto.ProfileMessage.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return new ProfileMessage(profileMessage.getResourceKey());
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.ProfileMessage.newBuilder()
                .setResourceKey(this.resourceKey)
                .build()
                .toByteArray();
    }
}
