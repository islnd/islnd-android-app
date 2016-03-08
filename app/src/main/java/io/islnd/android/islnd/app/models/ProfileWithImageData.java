package io.islnd.android.islnd.app.models;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.ProtoSerializable;
import io.islnd.android.islnd.messaging.proto.IslandProto;

import java.io.Serializable;

public class ProfileWithImageData implements Serializable, ProtoSerializable, VersionedContent {

    private final String displayName;
    private final String aboutMe;
    private final byte[] profileImageByteArray;
    private final byte[] headerImageByteArray;
    private final int version;

    public ProfileWithImageData(
            String displayName,
            String aboutMe,
            byte[] profileImageByteArray,
            byte[] headerImageByteArray,
            int version)
    {
        this.displayName = displayName;
        this.aboutMe = aboutMe;
        this.profileImageByteArray = profileImageByteArray;
        this.headerImageByteArray = headerImageByteArray;
        this.version = version;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getAboutMe()
    {
        return aboutMe;
    }

    public byte[] getProfileImageByteArray() {
        return profileImageByteArray;
    }

    public byte[] getHeaderImageByteArray() {
        return headerImageByteArray;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Profile.newBuilder()
                .setDisplayName(this.displayName)
                .setAboutMe(this.aboutMe)
                .setProfileImage(ByteString.copyFrom(this.profileImageByteArray))
                .setHeaderImage(ByteString.copyFrom(this.headerImageByteArray))
                .setVersion(this.version)
                .build()
                .toByteArray();
    }

    public static ProfileWithImageData fromProto(String string) {
        return fromProto(new Decoder().decode(string));
    }

    public static ProfileWithImageData fromProto(byte[] bytes) {
        // TODO: Change proto to have ProfileWithImageData
        IslandProto.Profile profile = null;
        try {
            profile = IslandProto.Profile.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return new ProfileWithImageData(
                profile.getDisplayName(),
                profile.getAboutMe(),
                profile.getProfileImage().toByteArray(),
                profile.getHeaderImage().toByteArray(),
                profile.getVersion());
    }
}
