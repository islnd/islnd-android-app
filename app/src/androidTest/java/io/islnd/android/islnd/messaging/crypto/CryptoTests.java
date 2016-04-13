package io.islnd.android.islnd.messaging.crypto;

import android.test.AndroidTestCase;

import java.lang.Exception;
import java.security.KeyPair;

import io.islnd.android.islnd.messaging.Identity;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class CryptoTests extends AndroidTestCase
{
    public void testObjectSigningWorks() throws Exception
    {
        Identity identity = bulidFakeIdentity();
        KeyPair keyPair = CryptoUtil.getKeyPair();
        SignedObject signedObject = CryptoUtil.sign(identity, keyPair.getPrivate());
        assertEquals(true, CryptoUtil.verifySignedObject(signedObject, keyPair.getPublic()));
    }

    private static Identity bulidFakeIdentity() {
        return new Identity(
                "display",
                "alias",
                "inbox",
                CryptoUtil.getKey(),
                CryptoUtil.getKeyPair().getPublic()
        );
    }
}