package io.islnd.android.islnd.app.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.security.Key;
import java.security.PublicKey;

import io.islnd.android.islnd.messaging.Identity;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

public class DataUtilsTest extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DatabaseTestHelpers.clearTables(mContext);
    }

    public void testUserIsUpdatedCorrectly() {
        //--Arrange
        PublicKey publicKey = CryptoUtil.getKeyPair().getPublic();
        long userId = DatabaseTestHelpers.insertFakeUser(mContext, publicKey);
        ContentValues values = new ContentValues();
        values.put(IslndContract.UserEntry.COLUMN_ACTIVE, IslndContract.UserEntry.NOT_ACTIVE);
        mContext.getContentResolver().update(
                IslndContract.UserEntry.CONTENT_URI,
                values,
                IslndContract.UserEntry._ID + " = ?",
                new String[] {Long.toString(userId)}
        );

        String[] projection = {
                IslndContract.UserEntry.COLUMN_ACTIVE,
        };
        Cursor cursor = mContext.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                IslndContract.UserEntry._ID + " = ?",
                new String[] {Long.toString(userId)},
                null
        );

        //--Verify deactivate worked
        assertTrue("user not found", cursor.moveToFirst());
        assertEquals("user is active but should have been de-activated", IslndContract.UserEntry.NOT_ACTIVE, cursor.getInt(0));
        cursor.close();

        String oldDisplayName = "rob";
        DatabaseTestHelpers.setDisplayName(mContext, userId, oldDisplayName);

        Key newGroupKey = CryptoUtil.getKey();
        String newDisplayName = "joey";
        String newAlias = "newAlias";
        String newInbox = "newInbox";
        Identity identity = new Identity(
                newDisplayName,
                newAlias,
                newInbox,
                newGroupKey,
                publicKey
        );

        String newOutbox = "newOutbox";

        DataUtils.activateAndUpdateUser(mContext, identity, newOutbox);

        //--Confirm user table is correct
        projection = new String[]{
                IslndContract.UserEntry.COLUMN_ACTIVE,
                IslndContract.UserEntry.COLUMN_MESSAGE_INBOX,
                IslndContract.UserEntry.COLUMN_MESSAGE_OUTBOX,
                IslndContract.UserEntry.COLUMN_PUBLIC_KEY
        };

        cursor = mContext.getContentResolver().query(
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
        assertEquals("wrong public key", CryptoUtil.encodeKey(publicKey), cursor.getString(3));

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
    }
}
