package io.islnd.android.islnd.messaging;

import android.util.Base64;

public class Encoder {
    public String encodeToString(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
