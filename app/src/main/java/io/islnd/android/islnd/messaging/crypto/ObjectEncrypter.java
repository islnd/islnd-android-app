package io.islnd.android.islnd.messaging.crypto;


import java.security.Key;

import io.islnd.android.islnd.messaging.Decoder;
import io.islnd.android.islnd.messaging.Encoder;
import io.islnd.android.islnd.messaging.ProtoSerializable;

public class ObjectEncrypter {
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
}
