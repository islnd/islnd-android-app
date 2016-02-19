package org.island.messaging.server;

import org.island.messaging.crypto.EncryptedProfile;

import java.util.List;

public class ProfileResponse {
    List<EncryptedProfile> profiles;

    public List<EncryptedProfile> getProfiles() {
        return profiles;
    }
}
