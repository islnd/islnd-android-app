package io.islnd.android.islnd.messaging.server;

import io.islnd.android.islnd.messaging.crypto.EncryptedProfile;

import java.util.List;

public class ProfileResponse {
    List<EncryptedProfile> profiles;

    public List<EncryptedProfile> getProfiles() {
        return profiles;
    }
}
