package com.island.island.Models;

import com.google.protobuf.InvalidProtocolBufferException;

import org.island.messaging.Decoder;
import org.island.messaging.ProtoSerializable;
import org.island.messaging.proto.IslandProto;

import java.io.Serializable;

public class ProfileWithImageData implements Serializable, ProtoSerializable, VersionedContent {

    private String username = "";
    private String aboutMe = "";
    private byte[] profileImageByteArray;
    private byte[] headerImageByteArray;
    private int version;

    public ProfileWithImageData(String username, String aboutMe, byte[] profileImageByteArray,
                                byte[] headerImageByteArray, int version)
    {
        this.username = username;
        this.aboutMe = aboutMe;
        this.profileImageByteArray = profileImageByteArray;
        this.headerImageByteArray = headerImageByteArray;
        this.version = version;
    }

    public String getUsername()
    {
        return username;
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
                .setUsername(this.username)
                .setAboutMe(this.aboutMe)
                .setProfileImage(this.profileImageByteArray.toString())
                .setHeaderImage(this.headerImageByteArray.toString())
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
                profile.getUsername(),
                profile.getAboutMe(),
                profile.getProfileImage().getBytes(),
                profile.getProfileImage().getBytes(),
                profile.getVersion());
    }
}
