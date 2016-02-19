package org.island.messaging.crypto;

import com.google.protobuf.InvalidProtocolBufferException;

import org.island.messaging.ProtoSerializable;
import org.island.messaging.proto.IslandProto;

public class SignedObject implements ProtoSerializable<SignedObject> {
    private final String object;
    private final String signature;

    public SignedObject(String object, String signature) {
        this.object = object;
        this.signature = signature;
    }

    public String getObject() {
        return object;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.SignedObject.newBuilder()
                .setObject(this.object)
                .setSignature(this.signature)
                .build()
                .toByteArray();
    }

    public static SignedObject fromProto(byte[] bytes) {
        IslandProto.SignedObject signedObject = null;
        try {
            signedObject = IslandProto.SignedObject.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return new SignedObject(signedObject.getObject(), signedObject.getSignature());
    }
}