package io.islnd.android.islnd.app.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.test.AndroidTestCase;

public class IslndProviderTests extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DatabaseTestHelpers.clearTables(mContext);
    }

    public void testFirstUserHasExpectedId() {
        long userId = DatabaseTestHelpers.insertFakeUser(mContext);
        assertEquals(userId, IslndContract.UserEntry.MY_USER_ID);
    }

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

    public void testUserUniqueConstraint() {
        final String key = "key";
        ContentValues originalValues = new ContentValues();
        final String originalInbox = "inbox1";
        originalValues.put(IslndContract.UserEntry.COLUMN_MESSAGE_INBOX, originalInbox);
        originalValues.put(IslndContract.UserEntry.COLUMN_PUBLIC_KEY, key);
        mContext.getContentResolver().insert(IslndContract.UserEntry.CONTENT_URI, originalValues);

        String[] projection = new String[] {IslndContract.UserEntry.COLUMN_MESSAGE_INBOX};
        Cursor cursor = mContext.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals(originalInbox, cursor.getString(0));

        ContentValues newValues = new ContentValues();
        final String newInbox = "inbox2";
        newValues.put(IslndContract.UserEntry.COLUMN_MESSAGE_INBOX, newInbox);
        newValues.put(IslndContract.UserEntry.COLUMN_PUBLIC_KEY, key);

        try {
            mContext.getContentResolver().insert(IslndContract.UserEntry.CONTENT_URI, newValues);
        } catch (Exception e) {
        }

        cursor = mContext.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        assertEquals(1, cursor.getCount());
        assertEquals(true, cursor.moveToFirst());
        assertEquals("inbox should not have changed!", originalInbox, cursor.getString(0));
    }

    public void testDisplayNameMustBeForExistingUser() {
        ContentValues values = new ContentValues();
        final int userId = 55;
        values.put(IslndContract.DisplayNameEntry.COLUMN_USER_ID, userId);
        final String displayName = "joey";
        values.put(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME, displayName);

        final ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = null;
        try {
            uri = contentResolver.insert(
                    IslndContract.DisplayNameEntry.CONTENT_URI,
                    values
            );
        } catch (Exception e) {
        }

        assertNull(uri);

        String[] projection = new String[] {BaseColumns._ID };
        Cursor cursor = contentResolver.query(
                IslndContract.DisplayNameEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        assertEquals(0, cursor.getCount());
    }

    public void testDisplayNameIsUpdatedOnMatchingInsert() {
        long userId = DatabaseTestHelpers.insertFakeUser(mContext);

        String oldDisplayName = "old";
        DatabaseTestHelpers.setDisplayName(mContext, userId, oldDisplayName);
        assertDisplayName(userId, oldDisplayName);

        String newDisplayName = "new";
        DatabaseTestHelpers.setDisplayName(mContext, userId, newDisplayName);
        assertDisplayName(userId, newDisplayName);
    }

    private void assertDisplayName(long userId, String displayName) {
        String[] projection = new String[] {IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME};
        Cursor cursor = mContext.getContentResolver().query(
                IslndContract.DisplayNameEntry.CONTENT_URI,
                projection,
                IslndContract.DisplayNameEntry.COLUMN_USER_ID + " = ?",
                new String[] { Long.toString(userId)},
                null
        );

        assertTrue(cursor.moveToFirst());
        assertEquals("display name is wrong!" + displayName, displayName, cursor.getString(0));
        cursor.close();
    }
}
