package io.islnd.android.islnd.app.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class IslndContract {

    public static final String CONTENT_AUTHORITY = "io.islnd.android.islnd.app.database";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_POST = "post";
    public static final String PATH_USER = "user";
    public static final String PATH_COMMENT = "comment";
    public static final String PATH_PROFILE = "profile";

    public static final class PostEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POST).build();

        public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POST;

        public static final String TABLE_NAME = "post";

        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_POST_ID = "post_id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_TIMESTAMP = "timestamp";

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

        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PSEUDONYM = "pseudonym";
        public static final String COLUMN_GROUP_KEY = "group_key";

        public static int getUserIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(2));
        }

        public static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
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

        public static final String COLUMN_POST_USER_ID = "post_user_id";
        public static final String COLUMN_POST_ID = "post_id";
        public static final String COLUMN_COMMENT_USER_ID = "comment_user_id";
        public static final String COLUMN_COMMENT_ID = "comment_id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_TIMESTAMP = "timestamp";

        public static Uri buildCommentUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static int getUserIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static String getPostIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static Uri buildCommentUriWithPostAuthorIdAndPostId(int postAuthorId, String postId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(postAuthorId))
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
}
