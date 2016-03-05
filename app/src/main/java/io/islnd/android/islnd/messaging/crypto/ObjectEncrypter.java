package io.islnd.android.islnd.messaging.crypto;


import com.google.protobuf.InvalidProtocolBufferException;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.Encoder;
import io.islnd.android.islnd.messaging.ProtoSerializable;
import io.islnd.android.islnd.messaging.PseudonymKey;
import io.islnd.android.islnd.messaging.proto.IslandProto;

import java.security.Key;
import java.util.Arrays;

public class ObjectEncrypter {
    private static final int CHUNK_SIZE = 190;
    private static final String DELIMITER = "_";
    private static Encoder encoder = new Encoder();
    private static Decoder decoder = new Decoder();


    public static String encryptSymmetric(ProtoSerializable o, Key key) {
        byte[] bytes = o.toByteArray();
        byte[] encryptedBytes = CryptoUtil.encryptSymmetric(bytes, key);
        return encoder.encodeToString(encryptedBytes);
    }

    public static byte[] decryptSymmetric(String string, Key key) {
        byte[] encryptedBytes = decoder.decode(string);
        return CryptoUtil.decryptSymmetric(encryptedBytes, key);
    }

    public static String encryptAsymmetric(PseudonymKey o, Key key) {
        IslandProto.PseudonymKey message = IslandProto.PseudonymKey.newBuilder()
                .setKey(CryptoUtil.encodeKey(o.getKey()))
                .setPseudonym(o.getPseudonym())
                .setUniqueId(o.getUniqueID())
                .setUsername(o.getPseudonym())
                .build();

        byte[][] chunks = getChunksFromObject(message.toByteArray());

        byte[][] encryptedChunks = new byte[chunks.length][];
        for (int i = 0; i < chunks.length; i++) {
            encryptedChunks[i] = CryptoUtil.encryptAsymmetricWithOAEP(chunks[i], key);
        }

        return combineChunksIntoString(encryptedChunks);
    }

    public static PseudonymKey decryptPseudonymKey(String string, Key key) {
        byte[][] encryptedChunks = getChunksFromString(string);

        byte[][] chunks = new byte[encryptedChunks.length][];
        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = CryptoUtil.decryptAsymmetricWithOAEP(encryptedChunks[i], key);
        }

        byte[] bytes = combineChunksIntoObject(chunks);
        try {
            IslandProto.PseudonymKey protoKey = IslandProto.PseudonymKey.parseFrom(bytes);
            PseudonymKey pseudonymKey = new PseudonymKey(
                    protoKey.getUniqueId(),
                    protoKey.getUsername(),
                    protoKey.getPseudonym(),
                    CryptoUtil.decodeSymmetricKey(protoKey.getKey()));
            return pseudonymKey;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] decryptAsymmetric(String string, Key key) {
        byte[][] encryptedChunks = getChunksFromString(string);

        byte[][] chunks = new byte[encryptedChunks.length][];
        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = CryptoUtil.decryptAsymmetricWithOAEP(encryptedChunks[i], key);
        }

        return combineChunksIntoObject(chunks);
    }

    private static byte[][] getChunksFromObject(byte[] serializedO) {
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
            sb.append(encoder.encodeToString(encryptedChunks[i]));
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
            chunks[i] = decoder.decode(tokens[i]);
        }

        return chunks;
    }

    private static byte[] combineChunksIntoObject(byte[][] chunks) {
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

        return object;
    }
}
