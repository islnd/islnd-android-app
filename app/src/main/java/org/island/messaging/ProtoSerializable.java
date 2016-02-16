package org.island.messaging;

public interface ProtoSerializable<T> {
    byte[] toByteArray();
}
