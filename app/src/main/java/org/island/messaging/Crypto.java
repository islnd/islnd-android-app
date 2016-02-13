package org.island.messaging;

import org.apache.commons.lang3.SerializationUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.Serializable;
import java.security.*;
import java.util.Arrays;

public class Crypto {

    private static final int IV_SIZE = 16;

    private static KeyPairGenerator keyPairGenerator;
    private static Cipher symmetricCipher;
    private static Cipher asymmetricCipherWithOAEP;
    private static Cipher asymmetricCipher;
    private static MessageDigest messageDigest;

    static {
        final String GENERATOR_ALGO = "RSA";
        final String SYMMETRIC_ALGO = "AES/CBC/PKCS5Padding";
        final String ASYMMETRIC_ALGO_WITH_OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
        final String ASYMMETRIC_ALGO = "RSA/ECB/PKCS1Padding";
        final String DIGEST_ALGO = "SHA-1";

        try {
            keyPairGenerator = KeyPairGenerator.getInstance(GENERATOR_ALGO);
            keyPairGenerator.initialize(2048);
            symmetricCipher = Cipher.getInstance(SYMMETRIC_ALGO);
            asymmetricCipherWithOAEP = Cipher.getInstance(ASYMMETRIC_ALGO_WITH_OAEP);
            asymmetricCipher = Cipher.getInstance(ASYMMETRIC_ALGO);
            messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
        } catch (GeneralSecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static KeyPair getKeyPair() {
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
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

    public static byte[] encryptAsymmetricWithOAEP(byte[] bytes, Key key) {
        try {
            asymmetricCipherWithOAEP.init(Cipher.ENCRYPT_MODE, key);
            return asymmetricCipherWithOAEP.doFinal(bytes);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return null;
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

    public static byte[] decryptAsymmetricWithOAEP(byte[] cipherText, Key key) {
        try {
            asymmetricCipherWithOAEP.init(Cipher.DECRYPT_MODE, key);
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

    public static <T extends Serializable> byte[] getDigest(T object) {
        return messageDigest.digest(SerializationUtils.serialize(object));
    }
}
