package io.islnd.android.islnd.messaging;

public interface ProtoSerializable<T> {
    byte[] toByteArray();
}
