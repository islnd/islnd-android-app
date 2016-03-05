package io.islnd.android.islnd.app.database;

import android.test.AndroidTestCase;

public class IslndProviderTests extends AndroidTestCase {
    public void testPostReturnsCorrectType() {
        String type = mContext.getContentResolver()
                .getType(IslndContract.PostEntry.CONTENT_URI);

        assertEquals(IslndContract.PostEntry.CONTENT_TYPE, type);
    }

    public void testUserReturnsCorrectType() {
        String type = mContext.getContentResolver()
                .getType(IslndContract.UserEntry.CONTENT_URI);

        assertEquals(IslndContract.UserEntry.CONTENT_TYPE, type);
    }

    public void testPostQuery() {

    }
}
