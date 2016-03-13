package io.islnd.android.islnd.messaging.event;

import java.io.Serializable;

import io.islnd.android.islnd.messaging.ProtoSerializable;

public abstract class Event implements
        Serializable,
        ProtoSerializable,
        Comparable<Event> {
    protected final String alias;
    protected final int eventId;

    protected Event(String alias, int eventId) {
        this.alias = alias;
        this.eventId = eventId;
    }

    public abstract int getType();

    public String getAlias() {
        return alias;
    }

    public int getEventId() {
        return eventId;
    }

    @Override
    public String toString() {
        return "EVENT: " + alias + " " + eventId;
    }

    @Override
    public int compareTo(Event another) {
        if (this.alias.equals(another.alias)) {
            return this.eventId - another.eventId;
        }

        return this.alias.compareTo(another.alias);
    }
}
