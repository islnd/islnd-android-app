package com.island.island.Models;

import com.google.protobuf.InvalidProtocolBufferException;

import org.island.messaging.Decoder;
import org.island.messaging.ProtoSerializable;
import org.island.messaging.proto.IslandProto;

import java.io.Serializable;

/**
 * Created by David Thompson on 12/21/2015.
 *
 * This class represents a user profile.
 */
public class Profile implements Serializable, ProtoSerializable, VersionedContent {

    private String username = "";
    private String aboutMe = "";
    private String profileImageUri = "";
    private String headerImageUri = "";
    private int version;

    public Profile(String username, String aboutMe, String profileImageUri, String headerImageUri,
                   int version)
    {
        this.username = username;
        this.aboutMe = aboutMe;
        this.profileImageUri = profileImageUri;
        this.headerImageUri = headerImageUri;
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

    public String getProfileImageUri() {
        return profileImageUri;
    }

    public String getHeaderImageUri() {
        return headerImageUri;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Profile.newBuilder()
                .setUsername(this.username)
                .setAboutMe(this.aboutMe)
                .setVersion(this.version)
                .build()
                .toByteArray();
    }

    public static Profile fromProto(String string) {
        return fromProto(new Decoder().decode(string));
    }

    public static Profile fromProto(byte[] bytes) {
        IslandProto.Profile profile = null;
        try {
            profile = IslandProto.Profile.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return new Profile(
                profile.getUsername(),
                profile.getAboutMe(),
                "",
                "",
                profile.getVersion());
    }
}
