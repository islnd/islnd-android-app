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
public class Profile implements Serializable, ProtoSerializable {

    private String username = "";
    private String aboutMe = "";

    public Profile(String username, String aboutMe)
    {
        this.username = username;
        this.aboutMe = aboutMe;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getAboutMe()
    {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe)
    {
        this.aboutMe = aboutMe;
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.Profile.newBuilder()
                .setUsername(this.username)
                .setAboutMe(this.aboutMe)
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

        return new Profile(profile.getUsername(), profile.getAboutMe());
    }
}
