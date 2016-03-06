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
    static final int USER = 200;
    static final int USER_WITH_ID = 201;
    static final int COMMENT = 300;
    static final int COMMENT_WITH_POST_USER_ID_AND_POST_ID = 301;
    static final int PROFILE = 400;
    static final int PROFILE_WITH_USER_ID = 401;

    private static final String sPostTableUserIdSelection =
            IslndContract.PostEntry.TABLE_NAME +
                    "." + IslndContract.PostEntry.COLUMN_USER_ID + " = ? ";

    private static final String sUserTableUserIdSelection =
            IslndContract.UserEntry.TABLE_NAME +
                    "." + IslndContract.UserEntry._ID + " = ? ";

    private static final SQLiteQueryBuilder sPostQueryBuilder;

    static {
        sPostQueryBuilder = new SQLiteQueryBuilder();

        sPostQueryBuilder.setTables(
                IslndContract.PostEntry.TABLE_NAME + " INNER JOIN " +
                        IslndContract.UserEntry.TABLE_NAME +
                        " ON " + IslndContract.PostEntry.TABLE_NAME + "."
                + IslndContract.PostEntry.COLUMN_USER_ID + " = " + IslndContract.UserEntry.TABLE_NAME
                + "." + IslndContract.UserEntry._ID
        );
    }

    private static final SQLiteQueryBuilder sCommentQueryBuilder;

    static {
        sCommentQueryBuilder = new SQLiteQueryBuilder();

        sCommentQueryBuilder.setTables(
                IslndContract.CommentEntry.TABLE_NAME + " INNER JOIN " + IslndContract.UserEntry.TABLE_NAME +
                        " ON " + IslndContract.CommentEntry.TABLE_NAME + "." + IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID +
                        " = " + IslndContract.UserEntry.TABLE_NAME + "." + IslndContract.UserEntry._ID
        );
    }

    private static final SQLiteQueryBuilder sProfileQueryBuilder;

    static {
        sProfileQueryBuilder = new SQLiteQueryBuilder();

        sProfileQueryBuilder.setTables(
                IslndContract.ProfileEntry.TABLE_NAME + " INNER JOIN " + IslndContract.UserEntry.TABLE_NAME +
                        " ON " + IslndContract.ProfileEntry.TABLE_NAME + "." + IslndContract.ProfileEntry.COLUMN_USER_ID +
                        " = " + IslndContract.UserEntry.TABLE_NAME + "." + IslndContract.UserEntry._ID
        );
    }

    private Cursor getPosts(Uri uri, String[] projection, String sortOrder) {
        return sPostQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
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
        Log.v(TAG, String.format("Get profile user id %d", userId));

        String[] selectionArgs = new String[] {Integer.toString(userId)};
        String selection = IslndContract.ProfileEntry.TABLE_NAME + "." +
                IslndContract.ProfileEntry.COLUMN_USER_ID + " = ?";

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

    private Cursor getCommentsByPostAuthorIdAndPostId(Uri uri, String[] projection, String sortOrder) {
        int userId = IslndContract.CommentEntry.getUserIdFromUri(uri);
        String postId = IslndContract.CommentEntry.getPostIdFromUri(uri);

        String selection = IslndContract.CommentEntry.COLUMN_POST_USER_ID + " = ? AND " +
                IslndContract.CommentEntry.COLUMN_POST_ID + " = ?";
        String[] selectionArgs = {Integer.toString(userId), postId};

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

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = IslndContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, IslndContract.PATH_POST, POST);
        matcher.addURI(authority, IslndContract.PATH_POST + "/#", POST_WITH_USER);

        matcher.addURI(authority, IslndContract.PATH_COMMENT, COMMENT);
        matcher.addURI(authority, IslndContract.PATH_COMMENT + "/#/*", COMMENT_WITH_POST_USER_ID_AND_POST_ID);

        matcher.addURI(authority, IslndContract.PATH_USER, USER);
        matcher.addURI(authority, IslndContract.PATH_USER + "/#", USER_WITH_ID);

        matcher.addURI(authority, IslndContract.PATH_PROFILE, PROFILE);
        matcher.addURI(authority, IslndContract.PATH_PROFILE + "/#", PROFILE_WITH_USER_ID);

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
        switch (sUriMatcher.match(uri)) {
            case POST: {
                retCursor = getPosts(uri, projection, sortOrder);
                break;
            }
            case POST_WITH_USER: {
                retCursor = getPostsByUserId(uri, projection, sortOrder);
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
            case COMMENT_WITH_POST_USER_ID_AND_POST_ID: {
                retCursor = getCommentsByPostAuthorIdAndPostId(uri, projection, sortOrder);
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
            case USER:
                return IslndContract.UserEntry.CONTENT_TYPE;
            case USER_WITH_ID:
                return IslndContract.UserEntry.CONTENT_ITEM_TYPE;
            case COMMENT:
                return IslndContract.CommentEntry.CONTENT_TYPE;
            case COMMENT_WITH_POST_USER_ID_AND_POST_ID:
                return IslndContract.CommentEntry.CONTENT_TYPE;
            case PROFILE:
                return IslndContract.ProfileEntry.CONTENT_TYPE;
            case PROFILE_WITH_USER_ID:
                return IslndContract.ProfileEntry.CONTENT_TYPE;
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
                        SQLiteDatabase.CONFLICT_IGNORE);
                if ( _id > 0 )
                    returnUri = IslndContract.PostEntry.buildPostUri(_id);
                else
                    return null;
                break;
            }
            case USER: {
                long _id = db.insert(IslndContract.UserEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = IslndContract.UserEntry.buildUserUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case COMMENT: {
                long _id = db.insertWithOnConflict(
                        IslndContract.CommentEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE);
                if ( _id > 0 ) {
                    Log.v(TAG, "inserted comment");
                    returnUri = IslndContract.CommentEntry.buildCommentUri(_id);
                }
                else {
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
                    Log.v(TAG, "inserted profile");
                    returnUri = IslndContract.ProfileEntry.buildProfileUri(_id);
                }
                else {
                    Log.v(TAG, "insert profile failed");
                    return null;
                }

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Log.v(TAG, "insert");
        getContext().getContentResolver().notifyChange(uri, null); // notify with base uri
        return returnUri;
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
            default:
                throw new UnsupportedOperationException("Bulk insert not supported for uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

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
            default: {
                throw new UnsupportedOperationException("update operation not supported for uri " + uri);
            }
        }

        return rowsUpdated;
    }
}
