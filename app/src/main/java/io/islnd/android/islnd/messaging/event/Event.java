package io.islnd.android.islnd.messaging.event;

import java.io.Serializable;

import io.islnd.android.islnd.messaging.ProtoSerializable;

public abstract class Event implements Serializable, ProtoSerializable {
    protected final String alias;

    protected Event(String alias) {
        this.alias = alias;
    }

    public abstract int getType();

    public String getAlias() {
        return alias;
    }
}
