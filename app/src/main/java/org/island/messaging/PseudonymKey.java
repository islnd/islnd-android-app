package org.island.messaging;

import com.google.protobuf.InvalidProtocolBufferException;

import org.island.messaging.crypto.CryptoUtil;
import org.island.messaging.proto.IslandProto;

import java.io.Serializable;
import java.security.Key;

public class PseudonymKey implements Serializable, ProtoSerializable<PseudonymKey> {
    private long uniqueID;
    private String username;
    private String pseudonym;

    //--TODO should we store the key as the string representation?
    private Key key;

    public long getUniqueID() {
        return uniqueID;
    }

    public String getPseudonym() {
        return pseudonym;
    }

    public Key getKey() {
        return key;
    }

    public PseudonymKey(long uniqueID, String username, String pseudonym, Key key) {
        this.uniqueID = uniqueID;
        this.username = username;
        this.pseudonym = pseudonym;
        this.key = key;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PseudonymKey)) {
            return false;
        }

        PseudonymKey otherKey = (PseudonymKey)other;
        return this.uniqueID == otherKey.uniqueID
                && this.pseudonym.equals(otherKey.pseudonym)
                && this.username.equals(otherKey.username)
                && this.key.equals(otherKey.key);
    }

    public boolean isNewer(PseudonymKey pseudonymKey) {
        return (uniqueID > pseudonymKey.uniqueID);
    }

    public String getUsername() {
        return this.username;
    }

    public static PseudonymKey fromProto(byte[] bytes) {
        IslandProto.PseudonymKey pseudonymKey = null;
        try {
            pseudonymKey = IslandProto.PseudonymKey.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            // TODO: Might want to handle for things like sms
            e.printStackTrace();
        }

        return new PseudonymKey(pseudonymKey.getUniqueId(),
                pseudonymKey.getUsername(),
                pseudonymKey.getPseudonym(),
                CryptoUtil.decodeSymmetricKey(pseudonymKey.getKey()));
    }

    @Override
    public byte[] toByteArray() {
        return IslandProto.PseudonymKey.newBuilder()
                .setKey(CryptoUtil.encodeKey(this.getKey()))
                .setPseudonym(this.getPseudonym())
                .setUniqueId(this.getUniqueID())
                .setUsername(this.getUsername())
                .build()
                .toByteArray();
    }
}
