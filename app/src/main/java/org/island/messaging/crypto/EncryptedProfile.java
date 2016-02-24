package org.island.messaging.crypto;

import com.island.island.Models.Profile;
import com.island.island.Models.ProfileWithImageData;

import org.island.messaging.PostUpdate;
import org.island.messaging.ProtoSerializable;

import java.security.Key;

public class EncryptedProfile extends SymmetricEncryptedData {
    public EncryptedProfile(ProfileWithImageData profile, Key privateKey, Key groupKey) {
        super(profile, privateKey, groupKey);
    }

    @Override
    public ProfileWithImageData decrypt(Key groupKey) {
        SignedObject signedObject = this.getSignedObject(groupKey);
        return ProfileWithImageData.fromProto(signedObject.getObject());
    }
}
