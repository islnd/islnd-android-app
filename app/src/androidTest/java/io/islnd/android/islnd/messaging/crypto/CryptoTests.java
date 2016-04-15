package io.islnd.android.islnd.messaging.crypto;

import android.test.AndroidTestCase;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import io.islnd.android.islnd.messaging.Identity;

public class CryptoTests extends AndroidTestCase
{
    public void testObjectSigningWorks() throws Exception
    {
        Identity identity = bulidFakeIdentity();
        KeyPair keyPair = CryptoUtil.getKeyPair();
        SignedObject signedObject = CryptoUtil.sign(identity, keyPair.getPrivate());
        assertEquals(true, CryptoUtil.verifySignedObject(signedObject, keyPair.getPublic()));
    }

    public void testKeyFingerprintsMatch() throws Exception {
        final BigInteger modulus = new BigInteger("572834619");
        final BigInteger exponent = new BigInteger("472856104");
        PublicKey publicKey1 = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
        PublicKey publicKey2 = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
        assertEquals(CryptoUtil.getDigest(publicKey1), CryptoUtil.getDigest(publicKey2));
    }

    public void testDifferentModulusHaveDifferentFingerprints() throws Exception {
        final BigInteger modulus1 = new BigInteger("572834619");
        final BigInteger modulus2 = new BigInteger("1118273");
        final BigInteger exponent = new BigInteger("472856104");
        PublicKey publicKey1 = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus1, exponent));
        PublicKey publicKey2 = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus2, exponent));
        assertFalse("fingerprints should not match", CryptoUtil.getDigest(publicKey1).equals(CryptoUtil.getDigest(publicKey2)));
    }

    public void testDifferentExponentsHaveDifferentFingerprints() throws Exception {
        final BigInteger modulus = new BigInteger("572834619");
        final BigInteger exponent1 = new BigInteger("472856104");
        final BigInteger exponent2 = new BigInteger("4747284104");
        PublicKey publicKey1 = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent1));
        PublicKey publicKey2 = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent2));
        assertFalse("fingerprints should not match", CryptoUtil.getDigest(publicKey1).equals(CryptoUtil.getDigest(publicKey2)));
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