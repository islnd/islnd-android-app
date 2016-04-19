package io.islnd.android.islnd.app.sms;

import android.test.AndroidTestCase;

import java.util.List;

public class MultipartMessageTests extends AndroidTestCase {

    public void testBaseCase() throws Exception {
        String message = "hello";
        List<String> result = MultipartMessage.buildMessages(message);
        assertEquals("should only be 1 message", 1, result.size());
    }

    public void testSplit() throws Exception {
        String message = "hellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohello";
        List<String> result = MultipartMessage.buildMessages(message);
        assertEquals("should be 2 messages", 2, result.size());
        assertTrue(result.get(0).length() <= MultipartMessage.MAX_LENGTH);
        assertTrue(result.get(1).length() <= MultipartMessage.MAX_LENGTH);
    }
}
