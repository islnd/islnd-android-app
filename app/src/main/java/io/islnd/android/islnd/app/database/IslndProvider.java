package io.islnd.android.islnd.app.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class IslndProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String TAG = IslndProvider.class.getSimpleName();
    private IslndDbHelper mOpenHelper;

    static final int POST = 100;
    static final int POST_WITH_USER = 101;
    static final int POST_WITH_USER_AND_POST_ID = 102;
    static final int USER = 200;
    static final int USER_WITH_ID = 201;
    static final int COMMENT = 300;
    static final int COMMENT_WITH_POST_AUTHOR_ALIAS_AND_POST_ID = 301;
    static final int PROFILE = 400;
    static final int PROFILE_WITH_USER_ID = 401;
    static final int ALIAS = 500;
    static final int ALIAS_WITH_USER_ID = 501;
    static final int DISPLAY_NAME = 600;
    static final int DISPLAY_NAME_WITH_USER_ID = 601;
    static final int IDENTITY = 700;
    static final int RECEIVED_EVENT = 800;
    static final int RECEIVED_EVENT_WITH_ALIAS_AND_EVENT_ID = 801;
    static final int OUTGOING_EVENT = 900;
    static final int RECEIVED_MESSAGE = 1000;
    static final int OUTGOING_MESSAGE = 1100;
    static final int NOTIFICATION = 1200;
    static final int NOTIFICATION_WITH_USER_DATA = 1300;
    static final int MESSAGE_TOKEN = 1400;
    static final int SMS_MESSAGE = 1500;
    static final int INVITE = 1600;
    static final int INVITE_WITH_ID = 1601;

    private static final String sPostTableUserIdSelection =
            IslndContract.PostEntry.TABLE_NAME +
                    "." + IslndContract.PostEntry.COLUMN_USER_ID + " = ? ";

    private static final String sPostTableUserIdAndPostIdSelection =
            IslndContract.PostEntry.TABLE_NAME
                    + "." + IslndContract.PostEntry.COLUMN_USER_ID + " = ? AND "
                    + IslndContract.PostEntry.TABLE_NAME
                    + "." + IslndContract.PostEntry.COLUMN_POST_ID + " = ? ";

    private static final String sUserTableUserIdSelection =
            IslndContract.UserEntry.TABLE_NAME +
                    "." + IslndContract.UserEntry._ID + " = ? ";

    private static final SQLiteQueryBuilder sPostQueryBuilder;

    static {
        sPostQueryBuilder = new SQLiteQueryBuilder();

        //--TODO this is probably too many inner joins
        //  Let's show that it is hurting performance and then let's fix it
        sPostQueryBuilder.setTables(
                IslndContract.PostEntry.TABLE_NAME + " INNER JOIN " + IslndContract.DisplayNameEntry.TABLE_NAME +
                        " ON " + IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_USER_ID +
                        " = " + IslndContract.DisplayNameEntry.TABLE_NAME + "." + IslndContract.DisplayNameEntry.COLUMN_USER_ID +
                        " INNER JOIN " + IslndContract.AliasEntry.TABLE_NAME +
                        " ON " + IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_USER_ID +
                        " = " + IslndContract.AliasEntry.TABLE_NAME + "." + IslndContract.AliasEntry.COLUMN_USER_ID +
                        " INNER JOIN " + IslndContract.ProfileEntry.TABLE_NAME +
                        " ON " + IslndContract.PostEntry.TABLE_NAME + "." + IslndContract.PostEntry.COLUMN_USER_ID +
                        " = " + IslndContract.ProfileEntry.TABLE_NAME + "." + IslndContract.ProfileEntry.COLUMN_USER_ID
        );
    }

    private static final SQLiteQueryBuilder sCommentQueryBuilder;

    static {
        sCommentQueryBuilder = new SQLiteQueryBuilder();

        sCommentQueryBuilder.setTables(
                IslndContract.CommentEntry.TABLE_NAME + " INNER JOIN " + IslndContract.DisplayNameEntry.TABLE_NAME +
                        " ON " + IslndContract.CommentEntry.TABLE_NAME + "." + IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID +
                        " = " + IslndContract.DisplayNameEntry.TABLE_NAME + "." + IslndContract.DisplayNameEntry.COLUMN_USER_ID +
                        " INNER JOIN " + IslndContract.ProfileEntry.TABLE_NAME +
                        " ON " + IslndContract.CommentEntry.TABLE_NAME + "." + IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID +
                        " = " + IslndContract.ProfileEntry.TABLE_NAME + "." + IslndContract.ProfileEntry.COLUMN_USER_ID
        );
    }

    private static final SQLiteQueryBuilder sProfileQueryBuilder;

    static {
        sProfileQueryBuilder = new SQLiteQueryBuilder();

        sProfileQueryBuilder.setTables(
                IslndContract.ProfileEntry.TABLE_NAME + " INNER JOIN " + IslndContract.DisplayNameEntry.TABLE_NAME +
                        " ON " + IslndContract.ProfileEntry.TABLE_NAME + "." + IslndContract.ProfileEntry.COLUMN_USER_ID +
                        " = " + IslndContract.DisplayNameEntry.TABLE_NAME + "." + IslndContract.DisplayNameEntry.COLUMN_USER_ID +
                        " INNER JOIN " + IslndContract.UserEntry.TABLE_NAME +
                        " ON " + IslndContract.ProfileEntry.TABLE_NAME + "." + IslndContract.ProfileEntry.COLUMN_USER_ID +
                        " = " + IslndContract.UserEntry.TABLE_NAME + "." + IslndContract.UserEntry._ID
        );
    }

    private static final SQLiteQueryBuilder sIdentityQueryBuilder;

    static {
        //--TODO this will not handle when users have multiple aliases
        sIdentityQueryBuilder = new SQLiteQueryBuilder();

        sIdentityQueryBuilder.setTables(
                IslndContract.UserEntry.TABLE_NAME + " INNER JOIN " + IslndContract.AliasEntry.TABLE_NAME +
                        " ON " + IslndContract.UserEntry.TABLE_NAME + "." + IslndContract.UserEntry._ID +
                        " = " + IslndContract.AliasEntry.TABLE_NAME + "." + IslndContract.AliasEntry.COLUMN_USER_ID +
                        " INNER JOIN " + IslndContract.DisplayNameEntry.TABLE_NAME +
                        " ON " + IslndContract.UserEntry.TABLE_NAME + "." + IslndContract.UserEntry._ID +
                        " = " + IslndContract.DisplayNameEntry.TABLE_NAME + "." + IslndContract.DisplayNameEntry.COLUMN_USER_ID
        );
    }

    private static final SQLiteQueryBuilder sNotificationWithUserDataQueryBuilder;

    static {
        sNotificationWithUserDataQueryBuilder = new SQLiteQueryBuilder();

        sNotificationWithUserDataQueryBuilder.setTables(
                IslndContract.NotificationEntry.TABLE_NAME + " INNER JOIN " + IslndContract.DisplayNameEntry.TABLE_NAME +
                        " ON " + IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_NOTIFICATION_USER_ID +
                        " = " + IslndContract.DisplayNameEntry.TABLE_NAME + "." + IslndContract.DisplayNameEntry.COLUMN_USER_ID +
                        " INNER JOIN " + IslndContract.ProfileEntry.TABLE_NAME +
                        " ON " + IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_NOTIFICATION_USER_ID +
                        " = " + IslndContract.ProfileEntry.TABLE_NAME + "." + IslndContract.ProfileEntry.COLUMN_USER_ID
        );
    }

    private Cursor getPostsByUserId(Uri uri, String[] projection, String sortOrder) {
        int userId = IslndContract.PostEntry.getUserIdFromUri(uri);

        String[] selectionArgs = new String[] {Integer.toString(userId)};
        String selection = sPostTableUserIdSelection;

        return sPostQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getPostsByUserIdAndPostId(Uri uri, String[] projection, String sortOrder) {
        int userId = IslndContract.PostEntry.getUserIdFromUri(uri);
        String postId = IslndContract.PostEntry.getPostIdFromUri(uri);

        String selection = sPostTableUserIdAndPostIdSelection;
        String[] selectionArgs = {Integer.toString(userId), postId};

        return sPostQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getDisplayNameById(Uri uri, String[] projection, String sortOrder) {
        int userId = IslndContract.DisplayNameEntry.getUserIdFromUri(uri);

        String[] selectionArgs = new String[] {Integer.toString(userId)};
        String selection = IslndContract.DisplayNameEntry.COLUMN_USER_ID + " = ?";

        return mOpenHelper.getReadableDatabase().query(
                IslndContract.DisplayNameEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getAliasesByUserId(Uri uri, String[] projection, String sortOrder) {
        int userId = IslndContract.AliasEntry.getUserIdFromUri(uri);

        String[] selectionArgs = new String[] {Integer.toString(userId)};
        String selection = IslndContract.AliasEntry.COLUMN_USER_ID + " = ?";

        return mOpenHelper.getReadableDatabase().query(
                IslndContract.AliasEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getUserByUserId(Uri uri, String[] projection, String sortOrder) {
        int userId = IslndContract.UserEntry.getUserIdFromUri(uri);

        String[] selectionArgs = new String[] {Integer.toString(userId)};
        String selection = sUserTableUserIdSelection;

        return mOpenHelper.getReadableDatabase().query(
                IslndContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getProfilesByUserId(Uri uri, String[] projection, String sortOrder) {
        int userId = IslndContract.ProfileEntry.getUserIdFromUri(uri);
        String[] selectionArgs = new String[] {Integer.toString(userId)};
        String selection = IslndContract.ProfileEntry.TABLE_NAME + "." +
                IslndContract.ProfileEntry.COLUMN_USER_ID + " = ?";

        return sProfileQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getInviteById(Uri uri, String[] projection) {
        Long inviteId = IslndContract.InviteEntry.getIdFromUri(uri);
        String selection = IslndContract.InviteEntry._ID + " = ?";
        String[] selectionArgs = {Long.toString(inviteId)};

        return mOpenHelper.getReadableDatabase().query(
                IslndContract.InviteEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    private Cursor getCommentsByPostAuthorAliasAndPostId(Uri uri, String[] projection, String sortOrder) {
        String postAuthorAlias = IslndContract.CommentEntry.getUserAliasFromUri(uri);
        String postId = IslndContract.CommentEntry.getPostIdFromUri(uri);

        String selection = IslndContract.CommentEntry.COLUMN_POST_AUTHOR_ALIAS + " = ? AND " +
                IslndContract.CommentEntry.COLUMN_POST_ID + " = ?";
        String[] selectionArgs = {postAuthorAlias, postId};

        return sCommentQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getEventByAliasAndUserId(Uri uri, String[] projection, String sortOrder) {
        String alias = IslndContract.ReceivedEventEntry.getAliasFromUri(uri);
        int eventId = IslndContract.ReceivedEventEntry.getEventIdFromUri(uri);

        String selection = IslndContract.ReceivedEventEntry.COLUMN_ALIAS + " = ? AND " +
                IslndContract.ReceivedEventEntry.COLUMN_EVENT_ID + " = ?";
        String[] selectionArgs = {
                alias,
                Integer.toString(eventId)
        };

        return mOpenHelper.getReadableDatabase().query(
                IslndContract.ReceivedEventEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private void updateProfileWithUserId(Uri uri, ContentValues values) {
        int userId = IslndContract.ProfileEntry.getUserIdFromUri(uri);

        String whereClause = IslndContract.ProfileEntry.COLUMN_USER_ID + " = ?";
        String[] whereArgs = { Integer.toString(userId) };

        mOpenHelper.getWritableDatabase().update(
                IslndContract.ProfileEntry.TABLE_NAME,
                values,
                whereClause,
                whereArgs);
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = IslndContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, IslndContract.PATH_POST, POST);
        matcher.addURI(authority, IslndContract.PATH_POST + "/#", POST_WITH_USER);
        matcher.addURI(authority, IslndContract.PATH_POST + "/#/*", POST_WITH_USER_AND_POST_ID);

        matcher.addURI(authority, IslndContract.PATH_COMMENT, COMMENT);
        matcher.addURI(authority, IslndContract.PATH_COMMENT + "/*/*",
                COMMENT_WITH_POST_AUTHOR_ALIAS_AND_POST_ID);

        matcher.addURI(authority, IslndContract.PATH_USER, USER);
        matcher.addURI(authority, IslndContract.PATH_USER + "/#", USER_WITH_ID);

        matcher.addURI(authority, IslndContract.PATH_PROFILE, PROFILE);
        matcher.addURI(authority, IslndContract.PATH_PROFILE + "/#", PROFILE_WITH_USER_ID);

        matcher.addURI(authority, IslndContract.PATH_ALIAS, ALIAS);
        matcher.addURI(authority, IslndContract.PATH_ALIAS + "/#", ALIAS_WITH_USER_ID);

        matcher.addURI(authority, IslndContract.PATH_DISPLAY_NAME, DISPLAY_NAME);
        matcher.addURI(authority, IslndContract.PATH_DISPLAY_NAME + "/#", DISPLAY_NAME_WITH_USER_ID);

        matcher.addURI(authority, IslndContract.PATH_IDENTITY, IDENTITY);

        matcher.addURI(authority, IslndContract.PATH_RECEIVED_EVENT, RECEIVED_EVENT);
        matcher.addURI(authority,
                IslndContract.PATH_RECEIVED_EVENT + "/*/#",
                RECEIVED_EVENT_WITH_ALIAS_AND_EVENT_ID);


        matcher.addURI(authority, IslndContract.PATH_OUTGOING_EVENT, OUTGOING_EVENT);

        matcher.addURI(authority, IslndContract.PATH_RECEIVED_MESSAGE, RECEIVED_MESSAGE);

        matcher.addURI(authority, IslndContract.PATH_OUTGOING_MESSAGE, OUTGOING_MESSAGE);

        matcher.addURI(authority, IslndContract.PATH_NOTIFICATION, NOTIFICATION);

        matcher.addURI(authority, IslndContract.PATH_NOTIFICATION_WITH_USER_DATA, NOTIFICATION_WITH_USER_DATA);

        matcher.addURI(authority, IslndContract.PATH_MESSAGE_TOKEN, MESSAGE_TOKEN);

        matcher.addURI(authority, IslndContract.PATH_SMS_MESSAGE, SMS_MESSAGE);

        matcher.addURI(authority, IslndContract.PATH_INVITE, INVITE);
        matcher.addURI(authority, IslndContract.PATH_INVITE + "/#", INVITE_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new IslndDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POST: {
                retCursor = sPostQueryBuilder.query(
                            mOpenHelper.getReadableDatabase(),
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                break;
            }
            case POST_WITH_USER: {
                retCursor = getPostsByUserId(uri, projection, sortOrder);
                break;
            }
            case POST_WITH_USER_AND_POST_ID: {
                retCursor = getPostsByUserIdAndPostId(uri, projection, sortOrder);
                break;
            }
            case USER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IslndContract.UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case USER_WITH_ID: {
                retCursor = getUserByUserId(uri, projection, sortOrder);
                break;
            }
            case COMMENT: {
                return sCommentQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
            }
            case COMMENT_WITH_POST_AUTHOR_ALIAS_AND_POST_ID: {
                retCursor = getCommentsByPostAuthorAliasAndPostId(uri, projection, sortOrder);
                break;
            }
            case PROFILE: {
                retCursor = sProfileQueryBuilder.query(
                            mOpenHelper.getReadableDatabase(),
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                break;
            }
            case PROFILE_WITH_USER_ID: {
                if (selection != null
                        || selectionArgs != null) {
                    throw new UnsupportedOperationException(
                            String.format("uri %s does not support selection or selection args")
                    );
                }
                retCursor = getProfilesByUserId(uri, projection, sortOrder);
                break;
            }
            case DISPLAY_NAME: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IslndContract.DisplayNameEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case DISPLAY_NAME_WITH_USER_ID: {
                retCursor = getDisplayNameById(uri, projection, sortOrder);
                break;
            }
            case IDENTITY: {
                retCursor = sIdentityQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case ALIAS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IslndContract.AliasEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case ALIAS_WITH_USER_ID: {
                retCursor = getAliasesByUserId(uri, projection, sortOrder);
                break;
            }
            case RECEIVED_EVENT_WITH_ALIAS_AND_EVENT_ID: {
                retCursor = getEventByAliasAndUserId(uri, projection, sortOrder);
                break;
            }
            case OUTGOING_EVENT: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IslndContract.OutgoingEventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case RECEIVED_MESSAGE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IslndContract.ReceivedMessageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case NOTIFICATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IslndContract.NotificationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case NOTIFICATION_WITH_USER_DATA: {
                retCursor = sNotificationWithUserDataQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case OUTGOING_MESSAGE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IslndContract.OutgoingMessageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case MESSAGE_TOKEN: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IslndContract.MessageTokenEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case SMS_MESSAGE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IslndContract.SmsMessageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case INVITE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        IslndContract.InviteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case INVITE_WITH_ID: {
                retCursor = getInviteById(uri, projection);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case POST:
                return IslndContract.PostEntry.CONTENT_TYPE;
            case POST_WITH_USER:
                return IslndContract.PostEntry.CONTENT_TYPE;
            case POST_WITH_USER_AND_POST_ID:
                return IslndContract.PostEntry.CONTENT_TYPE;
            case USER:
                return IslndContract.UserEntry.CONTENT_TYPE;
            case USER_WITH_ID:
                return IslndContract.UserEntry.CONTENT_ITEM_TYPE;
            case COMMENT:
                return IslndContract.CommentEntry.CONTENT_TYPE;
            case COMMENT_WITH_POST_AUTHOR_ALIAS_AND_POST_ID:
                return IslndContract.CommentEntry.CONTENT_TYPE;
            case PROFILE:
                return IslndContract.ProfileEntry.CONTENT_TYPE;
            case PROFILE_WITH_USER_ID:
                return IslndContract.ProfileEntry.CONTENT_ITEM_TYPE;
            case RECEIVED_EVENT:
                return IslndContract.ReceivedEventEntry.CONTENT_TYPE;
            case RECEIVED_EVENT_WITH_ALIAS_AND_EVENT_ID:
                return IslndContract.ReceivedEventEntry.CONTENT_ITEM_TYPE;
            case NOTIFICATION:
                return IslndContract.NotificationEntry.CONTENT_ITEM_TYPE;
            case NOTIFICATION_WITH_USER_DATA:
                return IslndContract.NotificationWithUserDataEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case POST: {
                long _id = db.insertWithOnConflict(
                        IslndContract.PostEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_FAIL);
                if ( _id > 0 )
                    returnUri = IslndContract.PostEntry.buildPostUri(_id);
                else
                    return null;
                break;
            }
            case USER: {
                long _id = db.insertWithOnConflict(
                        IslndContract.UserEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_FAIL);
                if ( _id > 0 ) {
                    returnUri = IslndContract.UserEntry.buildUserUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

                break;
            }
            case ALIAS: {
                long _id = db.insertWithOnConflict(
                        IslndContract.AliasEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_FAIL);
                if ( _id > 0 ) {
                    returnUri = IslndContract.AliasEntry.buildAliasUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

                break;
            }
            case DISPLAY_NAME: {
                long _id = db.insertWithOnConflict(
                        IslndContract.DisplayNameEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 ) {
                    returnUri = IslndContract.DisplayNameEntry.buildDisplayNameUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

                break;
            }
            case COMMENT: {
                long _id = db.insertWithOnConflict(
                        IslndContract.CommentEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_FAIL);
                if ( _id > 0 ) {
                    returnUri = IslndContract.CommentEntry.buildCommentUri(_id);
                    Log.v(TAG, "inserted comment");
                } else {
                    Log.v(TAG, "insert comment failed");
                    return null;
                }

                break;
            }
            case PROFILE: {
                long _id = db.insertWithOnConflict(
                        IslndContract.ProfileEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                if ( _id > 0 ) {
                    returnUri = IslndContract.ProfileEntry.buildProfileUri(_id);
                } else {
                    Log.v(TAG, "insert profile failed");
                    return null;
                }

                break;
            }
            case RECEIVED_EVENT: {
                long _id = db.insertWithOnConflict(
                        IslndContract.ReceivedEventEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_FAIL);
                if ( _id > 0 )
                    returnUri = IslndContract.ReceivedEventEntry.buildEventUri(_id);
                else
                    return null;
                break;
            }
            case RECEIVED_EVENT_WITH_ALIAS_AND_EVENT_ID: {
                returnUri = insertEventWithAliasAndUri(db, uri);
                break;
            }
            case OUTGOING_MESSAGE: {
                long _id = db.insert(IslndContract.OutgoingMessageEntry.TABLE_NAME, null, values);
                if ( _id > 0 ) {
                    returnUri = IslndContract.OutgoingMessageEntry.buildOutgoingMessageUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

                break;
            }
            case RECEIVED_MESSAGE: {
                long _id = db.insertWithOnConflict(
                        IslndContract.ReceivedMessageEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE);
                if ( _id > 0 )
                    returnUri = IslndContract.ReceivedEventEntry.buildEventUri(_id);
                else {
                    Log.d(TAG, "message already added to content provider");
                    return null;
                }

                break;
            }
            case NOTIFICATION: {
                long _id = db.insert(
                        IslndContract.NotificationEntry.TABLE_NAME,
                        null,
                        values);
                if ( _id > 0 ) {
                    getContext().getContentResolver().notifyChange(
                            IslndContract.NotificationWithUserDataEntry.CONTENT_URI,
                            null);
                    returnUri = IslndContract.NotificationEntry.buildNotificationUri(_id);
                } else {
                    return null;
                }

                break;
            }
            case MESSAGE_TOKEN: {
                long _id = db.insert(
                        IslndContract.MessageTokenEntry.TABLE_NAME,
                        null,
                        values);
                if ( _id > 0 ) {
                    returnUri = IslndContract.MessageTokenEntry.buildMessageTokenUri(_id);
                } else {
                    return null;
                }

                break;
            }
            case SMS_MESSAGE: {
                long _id = db.insert(
                        IslndContract.SmsMessageEntry.TABLE_NAME,
                        null,
                        values);
                if ( _id > 0 ) {
                    returnUri = IslndContract.SmsMessageEntry.buildSmsMessageUri(_id);
                } else {
                    return null;
                }

                break;
            }
            case INVITE: {
                long _id = db.insert(
                        IslndContract.InviteEntry.TABLE_NAME,
                        null,
                        values);
                if ( _id > 0 ) {
                    returnUri = IslndContract.InviteEntry.buildInviteUri(_id);
                } else {
                    return null;
                }

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Log.v(TAG, "inserted uri " + uri);
        getContext().getContentResolver().notifyChange(uri, null); // notify with base uri
        return returnUri;
    }

    private Uri insertEventWithAliasAndUri(SQLiteDatabase db, Uri uri) {
        ContentValues values = new ContentValues();
        values.put(
                IslndContract.ReceivedEventEntry.COLUMN_ALIAS,
                IslndContract.ReceivedEventEntry.getAliasFromUri(uri));
        values.put(
                IslndContract.ReceivedEventEntry.COLUMN_EVENT_ID,
                IslndContract.ReceivedEventEntry.getEventIdFromUri(uri));
        long _id = db.insertWithOnConflict(
                IslndContract.ReceivedEventEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE);
        if ( _id > 0 )
            return IslndContract.ReceivedEventEntry.buildEventUri(_id);
        else
            return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POST: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(IslndContract.PostEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }

                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case COMMENT: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(IslndContract.CommentEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case OUTGOING_EVENT: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(IslndContract.OutgoingEventEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case OUTGOING_MESSAGE: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(IslndContract.OutgoingMessageEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            default:
                throw new UnsupportedOperationException("Bulk insert not supported for uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        Log.v(TAG, "begin delete with uri " + uri);

        // this makes delete all rows return the number of rows deleted
        if (selection == null) {
            selection = "1";
        }

        switch (match) {
            case POST: {
                rowsDeleted = db.delete(
                        IslndContract.PostEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case POST_WITH_USER: {
                int userId = IslndContract.PostEntry.getUserIdFromUri(uri);
                rowsDeleted = db.delete(
                        IslndContract.PostEntry.TABLE_NAME,
                        sPostTableUserIdSelection,
                        new String[] {Integer.toString(userId)});
                break;
            }
            case COMMENT: {
                rowsDeleted = db.delete(
                        IslndContract.CommentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case USER: {
                rowsDeleted = db.delete(
                        IslndContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PROFILE: {
                rowsDeleted = db.delete(
                        IslndContract.ProfileEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case ALIAS: {
                rowsDeleted = db.delete(
                        IslndContract.AliasEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case DISPLAY_NAME: {
                rowsDeleted = db.delete(
                        IslndContract.DisplayNameEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case RECEIVED_EVENT: {
                rowsDeleted = db.delete(
                        IslndContract.ReceivedEventEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case OUTGOING_EVENT: {
                rowsDeleted = db.delete(
                        IslndContract.OutgoingEventEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case NOTIFICATION: {
                rowsDeleted = db.delete(
                        IslndContract.NotificationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case RECEIVED_MESSAGE: {
                rowsDeleted = db.delete(
                        IslndContract.ReceivedMessageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case OUTGOING_MESSAGE: {
                rowsDeleted = db.delete(
                        IslndContract.OutgoingMessageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case INVITE: {
                rowsDeleted = db.delete(
                        IslndContract.InviteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        Log.v(TAG, "begin update with uri " + uri);

        switch (match) {
            case PROFILE: {
                db.update(
                        IslndContract.ProfileEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case POST: {
                rowsUpdated = db.update(
                        IslndContract.PostEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                getContext().getContentResolver().notifyChange(IslndContract.PostEntry.CONTENT_URI, null);
                break;
            }
            case PROFILE_WITH_USER_ID: {
                updateProfileWithUserId(uri, values);
                getContext().getContentResolver().notifyChange(IslndContract.PostEntry.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(IslndContract.CommentEntry.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(IslndContract.NotificationWithUserDataEntry.CONTENT_URI, null);
                break;
            }
            case USER: {
                rowsUpdated = db.update(
                        IslndContract.UserEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                getContext().getContentResolver().notifyChange(IslndContract.UserEntry.CONTENT_URI, null);
                break;
            }
            case USER_WITH_ID: {
                int userId = IslndContract.UserEntry.getUserIdFromUri(uri);
                rowsUpdated = db.update(
                        IslndContract.UserEntry.TABLE_NAME,
                        values,
                        sUserTableUserIdSelection,
                        new String[] {Integer.toString(userId)}
                );
                getContext().getContentResolver().notifyChange(IslndContract.UserEntry.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(IslndContract.ProfileEntry.CONTENT_URI, null);
                break;
            }
            case DISPLAY_NAME_WITH_USER_ID: {
                String[] args = new String[] {
                        Integer.toString(IslndContract.DisplayNameEntry.getUserIdFromUri(uri))
                };

                db.update(
                        IslndContract.DisplayNameEntry.TABLE_NAME,
                        values,
                        IslndContract.DisplayNameEntry.COLUMN_USER_ID + " = ?",
                        args);
                getContext().getContentResolver().notifyChange(IslndContract.PostEntry.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(IslndContract.CommentEntry.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(IslndContract.ProfileEntry.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(IslndContract.NotificationWithUserDataEntry.CONTENT_URI, null);
                break;
            }
            case ALIAS_WITH_USER_ID: {
                String[] args = new String[] {
                        Integer.toString(IslndContract.AliasEntry.getUserIdFromUri(uri))
                };

                db.update(
                        IslndContract.AliasEntry.TABLE_NAME,
                        values,
                        IslndContract.AliasEntry.COLUMN_USER_ID + " = ?",
                        args);
                break;
            }
            case NOTIFICATION: {
                rowsUpdated = db.update(
                        IslndContract.NotificationEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                Log.v(TAG, "update notification updated " + rowsUpdated);
                getContext().getContentResolver().notifyChange(IslndContract.NotificationEntry.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(IslndContract.NotificationWithUserDataEntry.CONTENT_URI, null);
                break;
            }
            default: {
                throw new UnsupportedOperationException("update operation not supported for uri " + uri);
            }
        }

        getContext().getContentResolver().notifyChange(uri, null); // notify with base uri
        return rowsUpdated;
    }
}
