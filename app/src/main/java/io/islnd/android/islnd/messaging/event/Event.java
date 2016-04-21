package io.islnd.android.islnd.messaging.event;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.Serializable;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.ProtoSerializable;
import io.islnd.android.islnd.messaging.proto.IslandProto;

public abstract class Event implements
        Serializable,
        ProtoSerializable,
        Comparable<Event> {

    private static final String TAG = Event.class.getSimpleName();

    protected final String alias;
    protected final int eventId;
    protected final int eventType;

    //--This is a hack to defer comment processing
    private int userId;

    protected Event(String alias, int eventId, int eventType) {
        this.alias = alias;
        this.eventId = eventId;
        this.eventType = eventType;
    }

    public final int getType() {
        return this.eventType;
    }

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

    public static Event fromProto(String string) {
        IslandProto.Event event = null;
        byte[] bytes = new Decoder().decode(string);
        try {
            event = IslandProto.Event.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        switch (event.getEventType()) {
            case EventType.CHANGE_DISPLAY_NAME: {
                return new ChangeDisplayNameEvent(
                        event.getAlias(),
                        event.getEventId(),
                        event.getTextContent());
            }
            case EventType.NEW_POST: {
                return new NewPostEvent(
                        event.getAlias(),
                        event.getEventId(),
                        event.getContentId(),
                        event.getTextContent(),
                        event.getTimestamp());
            }
            case EventType.DELETE_POST: {
                return new DeletePostEvent(
                        event.getAlias(),
                        event.getEventId(),
                        event.getContentId());
            }
            case EventType.NEW_COMMENT: {
                return new NewCommentEvent(
                        event.getAlias(),
                        event.getEventId(),
                        event.getParentContentId(),
                        event.getParentAlias(),
                        event.getContentId(),
                        event.getTextContent(),
                        event.getTimestamp());
            }
            case EventType.DELETE_COMMENT: {
                return new DeleteCommentEvent(
                        event.getAlias(),
                        event.getEventId(),
                        event.getContentId());
            }
            case EventType.CHANGE_PROFILE_PICTURE: {
                return new ChangeProfilePictureEvent(
                        event.getAlias(),
                        event.getEventId(),
                        event.getDataContent().toByteArray());
            }
            case EventType.CHANGE_HEADER_PICTURE: {
                return new ChangeHeaderPictureEvent(
                        event.getAlias(),
                        event.getEventId(),
                        event.getDataContent().toByteArray());
            }
            case EventType.CHANGE_ABOUT_ME: {
                return new ChangeAboutMeEvent(
                        event.getAlias(),
                        event.getEventId(),
                        event.getTextContent());
            }
            case EventType.CHANGE_ALIAS: {
                return new ChangeAliasEvent(
                        event.getAlias(),
                        event.getEventId(),
                        event.getTextContent());
            }
            default: {
                Log.d(TAG, "Cannot recognize event of type " + event.getEventType());
            }
        }

        return null;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return this.userId;
    }
}
