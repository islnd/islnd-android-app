package io.islnd.android.islnd.messaging.crypto;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.Encoder;
import io.islnd.android.islnd.messaging.ProtoSerializable;

public class CryptoUtil {

    // encryption configurations
    private static final String SYMMETRIC_GENERATOR_ALGO = "AES";
    private static final String SYMMETRIC_ALGO = "AES/CBC/PKCS5Padding";
    private static final int SYMMETRIC_GENERATOR_LENGTH = 128;
    private static final int IV_SIZE = 16;

    private static final String ASYMMETRIC_GENERATOR_ALGO = "RSA";
    private static final String ASYMMETRIC_ALGO_WITH_OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int ASYMMETRIC_GENERATOR_LENGTH = 1024;
    public static final int ASYMMETRIC_BLOCK_SIZE = 60;

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    // encryption instances
    private static KeyGenerator keyGenerator;
    private static KeyPairGenerator keyPairGenerator;
    private static KeyFactory keyFactory;
    private static Cipher symmetricCipher;
    private static Cipher asymmetricCipherWithOAEP;
    private static SecureRandom secureRandom;
    private static Signature cryptoSignature;
    private static Encoder encoder = new Encoder();
    private static Decoder decoder = new Decoder();

    static {
        try {
            keyGenerator = KeyGenerator.getInstance(SYMMETRIC_GENERATOR_ALGO);
            keyGenerator.init(SYMMETRIC_GENERATOR_LENGTH);

            keyPairGenerator = KeyPairGenerator.getInstance(ASYMMETRIC_GENERATOR_ALGO);
            keyPairGenerator.initialize(ASYMMETRIC_GENERATOR_LENGTH);

            keyFactory = KeyFactory.getInstance(ASYMMETRIC_GENERATOR_ALGO);

            symmetricCipher = Cipher.getInstance(SYMMETRIC_ALGO);
            asymmetricCipherWithOAEP = Cipher.getInstance(ASYMMETRIC_ALGO_WITH_OAEP);

            cryptoSignature = Signature.getInstance(SIGNATURE_ALGORITHM);

            secureRandom = new SecureRandom();
        } catch (GeneralSecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Key getKey() {
        return keyGenerator.generateKey();
    }

    public static KeyPair getKeyPair() {
        return keyPairGenerator.generateKeyPair();
    }

    public static String encodeKey(Key key) {
        byte[] encodedKey = key.getEncoded();
        return encoder.encodeToString(encodedKey);
    }

    public static PrivateKey decodePrivateKey(String string) {
        byte[] encodedKey = decoder.decode(string);
        try {
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static PublicKey decodePublicKey(String string) {
        byte[] encodedKey = decoder.decode(string);
        try {
            return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Key decodeSymmetricKey(String string) {
        byte[] encodedKey = decoder.decode(string);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, SYMMETRIC_GENERATOR_ALGO);
    }

    public static byte[] encryptAsymmetricWithOAEP(byte[] bytes, Key key) {
        try {
            asymmetricCipherWithOAEP.init(Cipher.ENCRYPT_MODE, key,
                    new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT));
            return asymmetricCipherWithOAEP.doFinal(bytes);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] decryptAsymmetricWithOAEP(byte[] cipherText, Key key) {
        try {
            asymmetricCipherWithOAEP.init(Cipher.DECRYPT_MODE, key,
                    new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT));
            return asymmetricCipherWithOAEP.doFinal(cipherText);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] encryptSymmetric(byte[] bytes, Key key) {
        try {
            symmetricCipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] IV = symmetricCipher.getIV();
            byte[] encryptedBytes = symmetricCipher.doFinal(bytes);

            byte[] cipherText = new byte[IV.length + encryptedBytes.length];
            System.arraycopy(IV, 0, cipherText, 0, IV.length);
            System.arraycopy(encryptedBytes, 0, cipherText, IV.length, encryptedBytes.length);
            return cipherText;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] decryptSymmetric(byte[] cipherText, Key key) {
        byte[] IV = Arrays.copyOfRange(cipherText, 0, IV_SIZE);
        byte[] encryptedBytes = Arrays.copyOfRange(cipherText, IV_SIZE, cipherText.length);

        try {
            symmetricCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
            return symmetricCipher.doFinal(encryptedBytes);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String createAlias() {
        return String.valueOf(secureRandom.nextLong());
    }

    public static SignedObject sign(ProtoSerializable object, PrivateKey privateKey) {
        byte[] objectBytes = object.toByteArray();
        try {
            cryptoSignature.initSign(privateKey);
            cryptoSignature.update(objectBytes);
            byte[] objectSignature = cryptoSignature.sign();
            return new SignedObject(
                    encoder.encodeToString(object.toByteArray()),
                    encoder.encodeToString(objectSignature));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean verifySignedObject(SignedObject signedObject, PublicKey publicKey) {
        try {
            cryptoSignature.initVerify(publicKey);
            cryptoSignature.update(decoder.decode(signedObject.getObject()));
            return cryptoSignature.verify(decoder.decode(signedObject.getSignature()));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return false;
    }
}

