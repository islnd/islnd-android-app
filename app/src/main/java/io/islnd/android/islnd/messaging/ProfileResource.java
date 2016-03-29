package io.islnd.android.islnd.messaging;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import io.islnd.android.islnd.messaging.proto.IslandProto;

public class ProfileResource implements ProtoSerializable {
    private final String aboutMe;
    private final byte[] profileImageBytes;
    private final byte[] headerImageBytes;

    public ProfileResource(String aboutMe, byte[] profileImageBytes, byte[] headerImageBytes) {
        this.aboutMe = aboutMe;
        this.profileImageBytes = profileImageBytes;
        this.headerImageBytes = headerImageBytes;
    }

    public static ProfileResource fromProto(String string) {
        IslandProto.ProfileResource profileResource = null;
        byte[] bytes = new Decoder().decode(string);
        try {
            profileResource = IslandProto.ProfileResource.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return new ProfileResource(
                profileResource.getAboutMe(),
                profileResource.getProfileImage().toByteArray(),
                profileResource.getHeaderImage().toByteArray()
        );
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.ProfileResource.newBuilder()
                .setAboutMe(this.aboutMe)
                .setProfileImage(ByteString.copyFrom(this.profileImageBytes))
                .setHeaderImage(ByteString.copyFrom(this.headerImageBytes))
                .build()
                .toByteArray();
    }

    public byte[] getHeaderImageBytes() {
        return headerImageBytes;
    }

    public byte[] getProfileImageBytes() {
        return profileImageBytes;
    }

    public String getAboutMe() {
        return aboutMe;
    }
}
