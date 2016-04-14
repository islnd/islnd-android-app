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
}
