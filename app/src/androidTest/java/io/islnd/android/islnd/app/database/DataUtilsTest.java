package io.islnd.android.islnd.app.database;

import android.database.Cursor;
import android.test.AndroidTestCase;

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

    public void testUserIsUpdatedCorrectly() throws Exception {
        //--Arrange
        PublicKey publicKey = CryptoUtil.getKeyPair().getPublic();
        long userId = DatabaseTestHelpers.insertFakeUser(mContext, publicKey);
        DataUtils.deactivateUser(mContext, userId);

        String oldDisplayName = "rob";
        DatabaseTestHelpers.setDisplayName(mContext, userId, oldDisplayName);

        Key groupKey1 = CryptoUtil.getKey();
        String displayName1 = "joey";
        String alias1 = "alias";
        String inbox1 = "inbox";
        String outbox1 = "outbox";

        updateUserAndAssertUpdated(
                userId,
                groupKey1,
                displayName1,
                alias1,
                inbox1,
                outbox1);

        //--confirm only one record in alias table
        String[] projection = new String[]{
                IslndContract.AliasEntry._ID,
        };

        Cursor cursor = mContext.getContentResolver().query(
                IslndContract.AliasEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        assertEquals("should be one record", 1, cursor.getCount());
        cursor.close();

        Key groupKey2 = CryptoUtil.getKey();
        String displayName2 = "newName";
        String alias2 = "newAlias";
        String inbox2 = "newInbox";
        String outbox2 = "newOutbox";

        updateUserAndAssertUpdated(
                userId,
                groupKey2,
                displayName2,
                alias2,
                inbox2,
                outbox2);

        //--confirm only one record in alias table
        projection = new String[]{
                IslndContract.AliasEntry._ID,
        };

        cursor = mContext.getContentResolver().query(
                IslndContract.AliasEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        assertEquals("should be one record", 1, cursor.getCount());
        cursor.close();
    }

    private void updateUserAndAssertUpdated(long userId, Key newGroupKey, String newDisplayName, String newAlias, String newInbox, String newOutbox) {
        SecretIdentity secretIdentity = new SecretIdentity(
                newDisplayName,
                newAlias,
                newGroupKey
        );

        DataUtils.activateAndUpdateUser(mContext, (int) userId, secretIdentity, newInbox, newOutbox);

        //--Confirm user table is correct
        String[] projection = new String[]{
                IslndContract.UserEntry.COLUMN_ACTIVE,
                IslndContract.UserEntry.COLUMN_MESSAGE_INBOX,
                IslndContract.UserEntry.COLUMN_MESSAGE_OUTBOX,
                IslndContract.UserEntry.COLUMN_PUBLIC_KEY
        };

        Cursor cursor = mContext.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                IslndContract.UserEntry._ID + " = ?",
                new String[] {Long.toString(userId)},
                null
        );

        assertTrue("user not found", cursor.moveToFirst());
        assertEquals("user not active", IslndContract.UserEntry.ACTIVE, cursor.getInt(0));
        assertEquals("wrong inbox", newInbox, cursor.getString(1));
        assertEquals("wrong outbox", newOutbox, cursor.getString(2));

        cursor.close();

        //--Confirm alias table is correct
        projection = new String[]{
                IslndContract.AliasEntry.COLUMN_ALIAS,
                IslndContract.AliasEntry.COLUMN_GROUP_KEY,
        };

        cursor = mContext.getContentResolver().query(
                IslndContract.AliasEntry.CONTENT_URI,
                projection,
                IslndContract.AliasEntry.COLUMN_USER_ID + " = ?",
                new String[] {Long.toString(userId)},
                null
        );

        assertTrue("user not found", cursor.moveToFirst());
        assertEquals("alias incorrect", newAlias, cursor.getString(0));
        assertEquals("group key incorrect", CryptoUtil.encodeKey(newGroupKey), cursor.getString(1));
        cursor.close();
    }
}
