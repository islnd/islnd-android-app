package io.islnd.android.islnd.app.database;

import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.List;

import io.islnd.android.islnd.messaging.PublicKeyAndInbox;

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

    public void testNotificationWithUserId() throws Exception {
        long userId = DatabaseTestHelpers.insertFakeUser(mContext);
        DataUtils.insertNewFriendNotification(mContext, (int) userId);
        Cursor cursor = mContext.getContentResolver().query(
                IslndContract.NotificationEntry.CONTENT_URI,
                new String[] {IslndContract.NotificationEntry._ID },
                null,
                null,
                null);
        assertEquals(1, cursor.getCount());
    }

    public void testNotificationWithoutUserId() throws Exception {
        DataUtils.insertNewInviteNotification(mContext, 0);
        Cursor cursor = mContext.getContentResolver().query(
                IslndContract.NotificationEntry.CONTENT_URI,
                new String[] {IslndContract.NotificationEntry._ID },
                null,
                null,
                null);
        assertEquals(1, cursor.getCount());
    }

    public void testCanQueryDisplayNameWhenNoUserMatches() throws Exception {
        DataUtils.insertNewInviteNotification(mContext, 0);

        String[] projection = new String[]{
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry._ID,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_NOTIFICATION_USER_ID,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_NOTIFICATION_TYPE,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_POST_ID,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_TIMESTAMP,
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI
        };

        Cursor cursor = mContext.getContentResolver().query(
                IslndContract.NotificationWithUserDataEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertNull(cursor.getString(0));
    }

    public void testNotificationLeftJoinWorks() throws Exception {
        String displayName = "joe";
        long userId = DatabaseTestHelpers.insertFakeUser(mContext);
        DatabaseTestHelpers.setDisplayName(mContext, userId, displayName);
        DataUtils.insertNewFriendNotification(mContext, (int) userId);

        String[] projection = new String[]{
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry._ID,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_NOTIFICATION_USER_ID,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_NOTIFICATION_TYPE,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_POST_ID,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_TIMESTAMP,
                IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI
        };

        Cursor cursor = mContext.getContentResolver().query(
                IslndContract.NotificationWithUserDataEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(displayName, cursor.getString(0));
    }

    public void testGetsExpectedUsers() {
        long myUserId = DatabaseTestHelpers.insertFakeUser(mContext);
        assertEquals(IslndContract.UserEntry.MY_USER_ID, myUserId);
        long user1 = DatabaseTestHelpers.insertFakeUser(mContext);
        long user2 = DatabaseTestHelpers.insertFakeUser(mContext);
        List<PublicKeyAndInbox> result = DataUtils.getPublicKeyAndInboxForActiveUsersWithoutMeAndUserToRemove(mContext, (int) user1);
        assertEquals(1, result.size());

        DataUtils.deactivateUser(mContext, user2);
        result = DataUtils.getPublicKeyAndInboxForActiveUsersWithoutMeAndUserToRemove(mContext, (int) user1);
        assertEquals(0, result.size());
    }
}
