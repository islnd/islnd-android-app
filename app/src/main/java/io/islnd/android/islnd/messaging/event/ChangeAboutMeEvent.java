package io.islnd.android.islnd.messaging.event;

import io.islnd.android.islnd.messaging.proto.IslandProto;

public class ChangeAboutMeEvent extends Event {

    private final String aboutMe;

    protected ChangeAboutMeEvent(String alias, int eventId, String aboutMe) {
        super(alias, eventId, EventType.CHANGE_ABOUT_ME);

        this.aboutMe = aboutMe;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Event.newBuilder()
                .setAlias(this.alias)
                .setEventId(this.eventId)
                .setEventType(this.eventType)
                .setTextContent(this.aboutMe)
                .build()
                .toByteArray();
    }
}
