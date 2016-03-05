package io.islnd.android.islnd.messaging.crypto;

import io.islnd.android.islnd.app.models.ProfileWithImageData;

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
