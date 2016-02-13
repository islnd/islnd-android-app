package org.island.messaging;

import android.util.Base64;

import org.apache.commons.lang3.SerializationUtils;
import org.island.messaging.Crypto;

import java.io.Serializable;
import java.security.Key;
import java.util.Arrays;

public class ObjectEncrypter {
    private static final int CHUNK_SIZE = 190;
    private static final String DELIMITER = "_";

    public static String encryptSymmetric(Serializable o, Key key) {
        byte[] bytes = SerializationUtils.serialize(o);
        byte[] encryptedBytes = Crypto.encryptSymmetric(bytes, key);
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    public static Object decryptSymmetric(String string, Key key) {
        //byte[] encryptedBytes = decoder.decode(string);
        byte[] encryptedBytes = Base64.decode(string, Base64.DEFAULT);
        byte[] bytes = Crypto.decryptSymmetric(encryptedBytes, key);
        return SerializationUtils.deserialize(bytes);
    }

    public static String encryptAsymmetric(Serializable o, Key key) {
        byte[][] chunks = getChunksFromObject(o);

        byte[][] encryptedChunks = new byte[chunks.length][];
        for (int i = 0; i < chunks.length; i++) {
            encryptedChunks[i] = Crypto.encryptAsymmetricWithOAEP(chunks[i], key);
        }

        return combineChunksIntoString(encryptedChunks);
    }

    public static Object decryptAsymmetric(String string, Key key) {
        byte[][] encryptedChunks = getChunksFromString(string);

        byte[][] chunks = new byte[encryptedChunks.length][];
        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = Crypto.decryptAsymmetricWithOAEP(encryptedChunks[i], key);
        }

        return combineChunksIntoObject(chunks);
    }

    private static byte[][] getChunksFromObject(Serializable o) {
        byte[] serializedO = SerializationUtils.serialize(o);

        int numberOfChunks = (int) Math.ceil((double)serializedO.length / CHUNK_SIZE);
        byte[][] chunks = new byte[numberOfChunks][];
        for (int i = 0; i < numberOfChunks; i++) {
            int firstIndex = CHUNK_SIZE * i;
            int lastIndex = Math.min(serializedO.length, CHUNK_SIZE * (i + 1));
            chunks[i] = Arrays.copyOfRange(serializedO, firstIndex, lastIndex);
        }

        return chunks;
    }

    private static String combineChunksIntoString(byte[][] encryptedChunks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < encryptedChunks.length; i++) {
            sb.append(Base64.encodeToString(encryptedChunks[i], Base64.DEFAULT));
            if (i != encryptedChunks.length - 1) {
                sb.append(DELIMITER);
            }
        }

        return sb.toString();
    }

    private static byte[][] getChunksFromString(String string) {
        String[] tokens = string.split(DELIMITER);

        byte[][] chunks = new byte[tokens.length][];
        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = Base64.decode(tokens[i], Base64.DEFAULT);
        }

        return chunks;
    }

    private static Object combineChunksIntoObject(byte[][] chunks) {
        int totalLength = 0;
        for (int i = 0; i < chunks.length; i++) {
            totalLength += chunks[i].length;
        }

        byte[] object = new byte[totalLength];
        for (int i = 0; i < chunks.length; i++) {
            for (int j = 0; j < chunks[i].length; j++) {
                object[i * CHUNK_SIZE + j] = chunks[i][j];
            }
        }

        return SerializationUtils.deserialize(object);
    }
}
