package io.islnd.android.islnd.app.util;

import android.test.AndroidTestCase;

public class UtilTests extends AndroidTestCase {
    public void testColonFormatting() throws Exception {
        assertEquals("4", Util.formatWithColons("4"));
        assertEquals("45", Util.formatWithColons("45"));
        assertEquals("1:45", Util.formatWithColons("145"));
        assertEquals("a2:3b", Util.formatWithColons("a23b"));
        assertEquals("1:a2:3b", Util.formatWithColons("1a23b"));
    }

    public void testPartition() throws Exception {
        assertEquals("a", Util.partition("abcd", 4)[0]);
        assertEquals("b", Util.partition("abcd", 4)[1]);
        assertEquals("c", Util.partition("abcd", 4)[2]);
        assertEquals("d", Util.partition("abcd", 4)[3]);

        final String longString = "everydayeverynight";
        assertEquals(
                longString,
                Util.partition(longString, 4)[0]
                        + Util.partition(longString, 4)[1]
                        + Util.partition(longString, 4)[2]
                        + Util.partition(longString, 4)[3]);
    }
}
