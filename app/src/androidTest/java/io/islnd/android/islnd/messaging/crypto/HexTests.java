package io.islnd.android.islnd.messaging.crypto;

import android.test.AndroidTestCase;

import java.math.BigInteger;

public class HexTests extends AndroidTestCase {
    public void testHexIsCorrect() throws Exception {
        assertEquals("02", Hex.bytesToHex(BigInteger.valueOf(2).toByteArray()));
        assertEquals("11", Hex.bytesToHex(BigInteger.valueOf(17).toByteArray()));
        assertEquals("00962A", Hex.bytesToHex(BigInteger.valueOf(38442).toByteArray()));
    }
}
