package io.islnd.android.islnd.messaging.event;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.CommentKey;
import io.islnd.android.islnd.app.models.PostKey;
import io.islnd.android.islnd.app.util.ImageUtil;

public class EventProcessor {
    private static final String TAG = EventProcessor.class.getSimpleName();
    private static ContentResolver mContentResolver;

    public static void process(Context context, Event event) {
        Log.v(TAG, "processing " + event);
        mContentResolver = context.getContentResolver();
        if (alreadyProcessed(event)) {
            return;
        }

        int eventType = event.getType();
        switch (eventType) {
            case EventType.CHANGE_DISPLAY_NAME: {
                changeDisplayName(context, (ChangeDisplayNameEvent) event);
                break;
            }
            case EventType.NEW_POST: {
                addPost(context, (NewPostEvent) event);
                break;
            }
            case EventType.DELETE_POST: {
                deletePost(context, (DeletePostEvent) event);
                break;
            }
            case EventType.NEW_COMMENT: {
                addComment(context, (NewCommentEvent) event);
                break;
            }
            case EventType.DELETE_COMMENT: {
                deleteComment(context, (DeleteCommentEvent) event);
                break;
            }
            case EventType.CHANGE_PROFILE_PICTURE: {
                changeProfilePicture(context, (ChangeProfilePictureEvent) event);
                break;
            }
            case EventType.CHANGE_HEADER_PICTURE: {
                changeHeaderPicture(context, (ChangeHeaderPictureEvent) event);
                break;
            }
            case EventType.CHANGE_ABOUT_ME: {
                changeAboutMe(context, (ChangeAboutMeEvent) event);
                break;
            }
        }

        recordEventProcessed(event);
    }

    private static void changeHeaderPicture(Context context, ChangeHeaderPictureEvent event) {
        int userId = DataUtils.getUserIdFromAlias(context, event.getAlias());
        Uri headerPictureUri = ImageUtil.saveBitmapToInternalFromByteArray(
                context,
                event.getHeaderPicture());
        ContentValues values = new ContentValues();
        values.put(
                IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI,
                headerPictureUri.toString());
        mContentResolver.update(
                IslndContract.ProfileEntry.buildProfileUriWithUserId(userId),
                values,
                null,
                null
        );
    }

    private static void changeAboutMe(Context context, ChangeAboutMeEvent event) {
        int userId = DataUtils.getUserIdFromAlias(context, event.getAlias());
        ContentValues values = new ContentValues();
        values.put(IslndContract.ProfileEntry.COLUMN_ABOUT_ME, event.getAboutMe());
        mContentResolver.update(
                IslndContract.ProfileEntry.buildProfileUriWithUserId(userId),
                values,
                null,
                null
        );
    }

    private static void changeProfilePicture(Context context, ChangeProfilePictureEvent event) {
        int userId = DataUtils.getUserIdFromAlias(context, event.getAlias());
        Uri profilePictureUri = ImageUtil.saveBitmapToInternalFromByteArray(
                context,
                event.getProfilePicture());
        ContentValues values = new ContentValues();
        values.put(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI, profilePictureUri.toString());
        mContentResolver.update(
                IslndContract.ProfileEntry.buildProfileUriWithUserId(userId),
                values,
                null,
                null
        );
    }

    private static void changeDisplayName(Context context, ChangeDisplayNameEvent event) {
        int userId = DataUtils.getUserIdFromAlias(context, event.getAlias());
        ContentValues values = new ContentValues();
        values.put(
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME,
                event.getNewDisplayName());

        mContentResolver.update(
                IslndContract.DisplayNameEntry.buildDisplayNameWithUserId(userId),
                values,
                null,
                null
        );
    }

    private static void addPost(Context context, NewPostEvent newPostEvent) {
        int postUserId = DataUtils.getUserIdFromAlias(context, newPostEvent.getAlias());
        ContentValues values = new ContentValues();
        values.put(IslndContract.PostEntry.COLUMN_USER_ID, postUserId);
        values.put(IslndContract.PostEntry.COLUMN_POST_ID, newPostEvent.getPostId());
        values.put(IslndContract.PostEntry.COLUMN_CONTENT, newPostEvent.getContent());
        values.put(IslndContract.PostEntry.COLUMN_TIMESTAMP, newPostEvent.getTimestamp());
        context.getContentResolver().insert(
                IslndContract.PostEntry.CONTENT_URI,
                values);
    }

    private static void deletePost(Context context, DeletePostEvent deletePostEvent) {
        int postUserId = DataUtils.getUserIdFromAlias(context, deletePostEvent.getAlias());
        PostKey postToDelete = new PostKey(postUserId, deletePostEvent.getPostId());
        DataUtils.deletePost(context, postToDelete);
    }

    private static void addComment(Context context, NewCommentEvent newCommentEvent) {
//        int commentUserId = DataUtils.getUserIdFromAlias(context, newCommentEvent.getAlias());
//        int postUserId = DataUtils.getUserIdFromAlias(context, newCommentEvent.getPostAuthorAlias());
        int commentUserId = 100;
        int postUserId = 100;

        ContentValues values = new ContentValues();
        values.put(IslndContract.CommentEntry.COLUMN_POST_USER_ID, postUserId);
        values.put(IslndContract.CommentEntry.COLUMN_POST_ID, newCommentEvent.getPostId());
        values.put(IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID, commentUserId);
        values.put(IslndContract.CommentEntry.COLUMN_COMMENT_ID, newCommentEvent.getCommentId());
        values.put(IslndContract.CommentEntry.COLUMN_CONTENT, newCommentEvent.getContent());
        values.put(IslndContract.CommentEntry.COLUMN_TIMESTAMP, newCommentEvent.getTimestamp());
//        context.getContentResolver().insert(
//                IslndContract.CommentEntry.CONTENT_URI,
//                values);
    }

    private static void deleteComment(Context context, DeleteCommentEvent deleteCommentEvent) {
        int postUserId = DataUtils.getUserIdFromAlias(context, deleteCommentEvent.getAlias());
        CommentKey commentToDelete = new CommentKey(postUserId, deleteCommentEvent.getCommentId());
        DataUtils.deleteComment(context, commentToDelete);
    }

    private static void recordEventProcessed(Event event) {
        mContentResolver.insert(
                IslndContract.EventEntry.buildEventUriWithPseudonymAndEventId(event),
                new ContentValues()
        );
    }

    private static boolean alreadyProcessed(Event event) {
        String[] projection = new String[] {
                IslndContract.EventEntry._ID
        };

        Cursor cursor = null;
        boolean alreadyProcessed;
        try {
            cursor = mContentResolver.query(
                    IslndContract.EventEntry.buildEventUriWithPseudonymAndEventId(event),
                    projection,
                    null,
                    null,
                    null
            );

            alreadyProcessed = cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (alreadyProcessed) {
            Log.v(TAG, "already processed " + event);
        }

        return alreadyProcessed;
    }
}
