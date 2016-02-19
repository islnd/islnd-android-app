package org.island.messaging.crypto;

import com.island.island.Models.Profile;

import org.island.messaging.PostUpdate;
import org.island.messaging.ProtoSerializable;

import java.security.Key;

public class EncryptedProfile extends SymmetricEncryptedData {
    public EncryptedProfile(Profile profile, Key privateKey, Key groupKey) {
        super(profile, privateKey, groupKey);
    }

    @Override
    public Profile decrypt(Key groupKey) {
        SignedObject signedObject = this.getSignedObject(groupKey);
        return Profile.fromProto(signedObject.getObject());
    }
}
