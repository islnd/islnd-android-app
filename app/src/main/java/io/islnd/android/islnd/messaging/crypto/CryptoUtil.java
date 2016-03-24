package io.islnd.android.islnd.messaging.crypto;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.Encoder;
import io.islnd.android.islnd.messaging.ProtoSerializable;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {

    // encryption configurations
    private static final String SYMMETRIC_GENERATOR_ALGO = "AES";
    private static final String ASYMMETRIC_GENERATOR_ALGO = "RSA";
    private static final String SYMMETRIC_ALGO = "AES/CBC/PKCS5Padding";
    private static final String ASYMMETRIC_ALGO_WITH_OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String ASYMMETRIC_ALGO = "RSA/ECB/PKCS1Padding";
    private static final String DIGEST_ALGO = "SHA-1";
    private static final int SYMMETRIC_GENERATOR_LENGTH = 128;
    private static final int ASYMMETRIC_GENERATOR_LENGTH = 2048;
    private static final int IV_SIZE = 16;

    // encryption instances
    private static KeyGenerator keyGenerator;
    private static KeyPairGenerator keyPairGenerator;
    private static KeyFactory keyFactory;
    private static Cipher symmetricCipher;
    private static Cipher asymmetricCipherWithOAEP;
    private static Cipher asymmetricCipher;
    private static MessageDigest messageDigest;
    private static SecureRandom secureRandom;
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
            asymmetricCipher = Cipher.getInstance(ASYMMETRIC_ALGO);
            messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
            secureRandom = new SecureRandom();
        } catch (GeneralSecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static String alias;

    public static Key getKey() {
        return keyGenerator.generateKey();
    }

    public static KeyPair getKeyPair() {
        return keyPairGenerator.generateKeyPair();
    }

    public static byte[] encryptAsymmetric(byte[] bytes, Key key) {
        try {
            asymmetricCipher.init(Cipher.ENCRYPT_MODE, key);
            return asymmetricCipher.doFinal(bytes);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String encodeKey(Key key) {
        byte[] encodedKey = key.getEncoded();
        return encoder.encodeToString(encodedKey);
    }

    public static Key decodePrivateKey(String string) {
        byte[] encodedKey = decoder.decode(string);
        try {
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Key decodePublicKey(String string) {
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

    public static byte[] getDigest(ProtoSerializable object) {
        return messageDigest.digest(object.toByteArray());
    }

    public static byte[] getDigest(String object) {
        return messageDigest.digest(decoder.decode(object));
    }

    public static byte[] decryptAsymmetric(byte[] cipherText, Key key) throws DecryptionErrorException {
        try {
            asymmetricCipher.init(Cipher.DECRYPT_MODE, key);
            return asymmetricCipher.doFinal(cipherText);
        } catch (BadPaddingException e) {
            throw new DecryptionErrorException();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String createAlias() {
        return String.valueOf(secureRandom.nextLong());
    }
}

