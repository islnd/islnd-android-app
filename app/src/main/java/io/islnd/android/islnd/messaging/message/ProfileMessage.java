package io.islnd.android.islnd.messaging.message;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.ProtoSerializable;
import io.islnd.android.islnd.messaging.proto.IslandProto;

public class ProfileMessage implements ProtoSerializable<ProfileMessage> {
    private final String aboutMe;
    private final byte[] profileImageBytes;
    private final byte[] headerImageBytes;

    public ProfileMessage(String aboutMe, byte[] profileImageBytes, byte[] headerImageBytes) {
        this.aboutMe = aboutMe;
        this.profileImageBytes = profileImageBytes;
        this.headerImageBytes = headerImageBytes;
    }

    public static ProfileMessage fromProto(String encodedBytes) {
        IslandProto.ProfileMessage profileMessage = null;
        byte[] bytes = new Decoder().decode(encodedBytes);
        try {
            profileMessage = IslandProto.ProfileMessage.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return new ProfileMessage(
                profileMessage.getAboutMe(),
                profileMessage.getProfileImage().toByteArray(),
                profileMessage.getHeaderImage().toByteArray());
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public byte[] getProfileImageBytes() {
        return profileImageBytes;
    }

    public byte[] getHeaderImageBytes() {
        return headerImageBytes;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.ProfileMessage.newBuilder()
                .setAboutMe(this.aboutMe)
                .setProfileImage(ByteString.copyFrom(this.profileImageBytes))
                .setHeaderImage(ByteString.copyFrom(this.headerImageBytes))
                .build()
                .toByteArray();
    }
}
