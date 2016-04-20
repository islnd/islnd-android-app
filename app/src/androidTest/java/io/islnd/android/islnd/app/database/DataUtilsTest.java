package io.islnd.android.islnd.app.database;

import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;

import java.security.Key;
import java.security.PublicKey;

import io.islnd.android.islnd.messaging.SecretIdentity;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

public class DataUtilsTest extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DatabaseTestHelpers.clearTables(mContext);
    }

    public void testDeactivateUser() throws Exception {
        long userId = DatabaseTestHelpers.insertFakeUser(mContext);
        assertTrue("user does not exist", DataUtils.userExists(mContext, userId));
        assertTrue("user was not activate", DataUtils.isUserActive(mContext, userId));
        DataUtils.deactivateUser(mContext, userId);
        assertTrue("user does not exist", DataUtils.userExists(mContext, userId));
        assertFalse("user was not deactivated", DataUtils.isUserActive(mContext, userId));
    }
}
