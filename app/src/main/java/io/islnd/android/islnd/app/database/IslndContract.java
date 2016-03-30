package io.islnd.android.islnd.app.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import io.islnd.android.islnd.messaging.event.Event;

public class IslndContract {

    public static final String CONTENT_AUTHORITY = "io.islnd.android.islnd.app.database";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_USER = "user";
    public static final String PATH_ALIAS = "alias";
    public static final String PATH_DISPLAY_NAME = "display_name";
    public static final String PATH_POST = "post";
    public static final String PATH_COMMENT = "comment";
    public static final String PATH_PROFILE = "profile";
    public static final String PATH_IDENTITY = "identity";
    public static final String PATH_RECEIVED_EVENT = "received_event";
    public static final String PATH_OUTGOING_EVENT = "outgoing_event";
    public static final String PATH_RECEIVED_MESSAGE = "received_message";
    public static final String PATH_OUTGOING_MESSAGE = "outgoing_message";
    public static final String PATH_MAILBOX = "mailbox";

    public static final class PostEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POST;

        public static final String TABLE_NAME = "post";

        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_ALIAS = "alias";
        public static final String COLUMN_POST_ID = "post_id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_COMMENT_COUNT = "comment_count";

        public static int getUserIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static Uri buildPostUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPostUriWithUserId(int userId) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(userId)).build();
        }
    }

    public static final class UserEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;

        public static final String TABLE_NAME = "user";

        public static final String COLUMN_PUBLIC_KEY = "public_key";
        public static final String COLUMN_MESSAGE_INBOX = "message_inbox";
        public static final String COLUMN_MESSAGE_OUTBOX = "message_outbox";

        public static final int MY_USER_ID = 1; //--we are always the first user to go in the database

        public static int getUserIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUserWithUserId(int userId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(userId))
                    .build();
        }
    }

    public static final class DisplayNameEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DISPLAY_NAME).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DISPLAY_NAME;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DISPLAY_NAME;

        public static final String TABLE_NAME = "display_name";

        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_DISPLAY_NAME = "display_name";

        public static int getUserIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static Uri buildDisplayNameUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildDisplayNameWithUserId(int userId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(userId))
                    .build();
        }
    }

    public static final class AliasEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ALIAS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALIAS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALIAS;

        public static final String TABLE_NAME = "alias";

        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_ALIAS = "alias";
        public static final String COLUMN_GROUP_KEY = "group_key";

        public static int getUserIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static Uri buildAliasUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildAliasWithUserId(int userId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(userId))
                    .build();
        }
    }

    public static final class CommentEntry implements BaseColumns {
        private static final String TAG = CommentEntry.class.getSimpleName();

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COMMENT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMENT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMENT;

        public static final String TABLE_NAME = "comment";

        public static final String COLUMN_POST_AUTHOR_ALIAS = "post_alias";
        public static final String COLUMN_POST_ID = "post_id";
        public static final String COLUMN_COMMENT_USER_ID = "comment_user_id";
        public static final String COLUMN_COMMENT_ID = "comment_id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_TIMESTAMP = "timestamp";

        public static Uri buildCommentUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getUserAliasFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getPostIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static Uri buildCommentUriWithPostAuthorIdAndPostId(String postAuthorAlias, String postId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(postAuthorAlias)
                    .appendPath(postId)
                    .build();
        }
    }

    public static final class ProfileEntry implements BaseColumns {
        private static final String TAG = ProfileEntry.class.getSimpleName();

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROFILE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROFILE;

        public static final String TABLE_NAME = "profile";

        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_ABOUT_ME = "about_me";
        public static final String COLUMN_PROFILE_IMAGE_URI = "profile_image_uri";
        public static final String COLUMN_HEADER_IMAGE_URI = "header_image_uri";

        public static Uri buildProfileUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static int getUserIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static Uri buildProfileUriWithUserId(int userId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(userId))
                    .build();
        }
    }

    public static final class ReceivedEventEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECEIVED_EVENT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECEIVED_EVENT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECEIVED_EVENT;

        public static final String TABLE_NAME = "received_event";

        public static final String COLUMN_ALIAS = "alias";
        public static final String COLUMN_EVENT_ID = "event_id";

        public static String getAliasFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static int getEventIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(2));
        }

        public static Uri buildEventUriWithPseudonymAndEventId(Event event) {
            return CONTENT_URI.buildUpon()
                    .appendPath(event.getAlias())
                    .appendPath(Integer.toString(event.getEventId()))
                    .build();
        }

        public static Uri buildEventUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class OutgoingEventEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_OUTGOING_EVENT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_OUTGOING_EVENT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_OUTGOING_EVENT;

        public static final String TABLE_NAME = "outgoing_event";

        public static final String COLUMN_ALIAS = "alias";
        public static final String COLUMN_BLOB = "blob";
    }

    public static final class ReceivedMessageEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECEIVED_MESSAGE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECEIVED_MESSAGE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECEIVED_MESSAGE;

        public static final String TABLE_NAME = "received_message";

        public static final String COLUMN_MAILBOX = "mailbox";
        public static final String COLUMN_MESSAGE_ID = "message_id";

        public static Uri buildReceivedMessageUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class OutgoingMessageEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_OUTGOING_MESSAGE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_OUTGOING_MESSAGE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_OUTGOING_MESSAGE;

        public static final String TABLE_NAME = "outgoing_message";

        public static final String COLUMN_MAILBOX = "mailbox";
        public static final String COLUMN_BLOB = "blob";
    }
}
