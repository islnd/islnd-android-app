package io.islnd.android.islnd.messaging.event;

import com.google.protobuf.InvalidProtocolBufferException;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.ProtoSerializable;
import io.islnd.android.islnd.messaging.proto.IslandProto;

public class ChangeDisplayNameEvent extends Event {

    private final String newDisplayName;

    protected ChangeDisplayNameEvent(String alias, String newDisplayName) {
        super(alias);
        this.newDisplayName = newDisplayName;
    }

    @Override
    public int getType() {
        return EventType.CHANGE_DISPLAY_NAME;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.ChangeDisplayNameEvent.newBuilder()
                .setDisplayName(this.newDisplayName)
                .setAlias(this.alias)
                .build()
                .toByteArray();
    }

    public String getNewDisplayName() {
        return newDisplayName;
    }

    public static ChangeDisplayNameEvent fromProto(String string) {
        IslandProto.ChangeDisplayNameEvent changeDisplayNameEvent = null;
        byte[] bytes = new Decoder().decode(string);
        try {
            changeDisplayNameEvent = IslandProto.ChangeDisplayNameEvent.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return new ChangeDisplayNameEvent(
                changeDisplayNameEvent.getAlias(),
                changeDisplayNameEvent.getDisplayName());
    }
}
