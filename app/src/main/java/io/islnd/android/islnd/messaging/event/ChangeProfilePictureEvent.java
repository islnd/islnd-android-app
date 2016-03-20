package io.islnd.android.islnd.messaging.event;

import com.google.protobuf.ByteString;

import io.islnd.android.islnd.messaging.proto.IslandProto;

public class ChangeProfilePictureEvent extends Event {

    private final byte[] profilePicture;

    protected ChangeProfilePictureEvent(String alias, int eventId, byte[] profilePicture) {
        super(alias, eventId, EventType.CHANGE_PROFILE_PICTURE);

        this.profilePicture = profilePicture;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Event.newBuilder()
                .setAlias(this.alias)
                .setEventId(this.eventId)
                .setEventType(this.eventType)
                .setDataContent(ByteString.copyFrom(this.profilePicture))
                .build()
                .toByteArray();
    }
}
