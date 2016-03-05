package io.islnd.android.islnd.messaging;

import android.util.Base64;

public class Decoder {
    public byte[] decode(String string) {
        return Base64.decode(string, Base64.NO_WRAP);
    }
}
