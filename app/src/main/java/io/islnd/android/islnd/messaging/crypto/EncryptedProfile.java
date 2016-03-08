package io.islnd.android.islnd.messaging.crypto;

import io.islnd.android.islnd.app.models.ProfileWithImageData;

import java.security.Key;

public class EncryptedProfile extends SymmetricEncryptedData {
    public EncryptedProfile(ProfileWithImageData profile, Key privateKey, Key groupKey) {
        super(profile, privateKey, groupKey);
    }

    @Override
    public ProfileWithImageData decryptAndVerify(Key groupKey, Key publicKey) throws InvalidSignatureException {
        SignedObject signedObject = this.getSignedAndVerifiedObject(groupKey, publicKey);
        return ProfileWithImageData.fromProto(signedObject.getObject());
    }
}
