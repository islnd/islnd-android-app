package io.islnd.android.islnd.messaging;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.crypto.EncryptedComment;
import io.islnd.android.islnd.messaging.crypto.EncryptedPost;
import io.islnd.android.islnd.messaging.server.CommentQuery;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.database.ProfileDatabase;
import io.islnd.android.islnd.app.models.Post;
import io.islnd.android.islnd.app.models.PostKey;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.models.ProfileWithImageData;
import io.islnd.android.islnd.app.models.User;
import io.islnd.android.islnd.app.PostCollection;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.app.VersionedContentBuilder;

import io.islnd.android.islnd.messaging.crypto.EncryptedData;
import io.islnd.android.islnd.messaging.crypto.EncryptedProfile;
import io.islnd.android.islnd.messaging.crypto.ObjectEncrypter;
import io.islnd.android.islnd.messaging.server.CommentQueryRequest;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class MessageLayer {
    private static final String TAG = MessageLayer.class.getSimpleName();

    public static List<User> getReaders(Context context, String username, Key privateKey) {
        //call the REST service
        List<EncryptedData> keys = Rest.getReaders(username, io.islnd.android.islnd.app.util.Util.getApiKey(context));
        if (keys == null) {
            Log.d(TAG, "get readers returned null");
            return new ArrayList<>();
        }

        //decrypt the friends and addPost to DB
        List<User> friends = new ArrayList<>();
        for (EncryptedData encryptedPseudonymKey : keys) {
            PseudonymKey pseudonymKey = ObjectEncrypter.decryptPseudonymKey(
                    encryptedPseudonymKey.getBlob(),
                    privateKey);
            addFriendToDatabaseAndCreateDefaultProfile(context, pseudonymKey);
            friends.add(new User(pseudonymKey.getUsername()));
        }

        return friends;
    }

    public static void postPublicKey(Context context, String username, Key publicKey){
        Rest.postPublicKey(username, CryptoUtil.encodeKey(publicKey), io.islnd.android.islnd.app.util.Util.getApiKey(context));
    }

    public static PostCollection getPosts(Context context) {

        List<PseudonymKey> keys = getPseudonymKeys(context);
        PostCollection postCollection = new PostCollection();
        String apiKey = Util.getApiKey(context);

        for (PseudonymKey friendPseudonymKey: keys) {
            List<EncryptedPost> encryptedPosts = Rest.getPosts(
                    friendPseudonymKey.getPseudonym(),
                    apiKey);
            if (encryptedPosts == null) {
                continue;
            }

            int friendUserId = DataUtils.getUserId(context, friendPseudonymKey.getUsername());
            for (EncryptedPost encryptedPost: encryptedPosts) {
                PostUpdate postUpdate = encryptedPost.decrypt(friendPseudonymKey.getKey());
                if (postUpdate == null) {
                    continue;
                }

                //--TODO check that post is signed
                addPostToCollection(
                        postCollection,
                        friendPseudonymKey.getUsername(),
                        friendUserId,
                        postUpdate);
            }
        }

        addPostsToContentProvider(context, postCollection);
        return postCollection;
    }

    private static List<PseudonymKey> getPseudonymKeys(Context context) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_USERNAME,
                IslndContract.UserEntry.COLUMN_PSEUDONYM,
                IslndContract.UserEntry.COLUMN_GROUP_KEY,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        List<PseudonymKey> pseudonymKeys = new ArrayList<>();
        if (!cursor.moveToFirst()) {
            return pseudonymKeys;
        }

        do {
            String username = cursor.getString(cursor.getColumnIndex(IslndContract.UserEntry.COLUMN_USERNAME));
            String pseudonym = cursor.getString(cursor.getColumnIndex(IslndContract.UserEntry.COLUMN_PSEUDONYM));
            Key groupKey = CryptoUtil.decodeSymmetricKey(
                    cursor.getString(cursor.getColumnIndex(IslndContract.UserEntry.COLUMN_GROUP_KEY)));
            pseudonymKeys.add(new PseudonymKey(
                            1,
                            username,
                            pseudonym,
                            groupKey
                    ));
        } while (cursor.moveToNext());

        return pseudonymKeys;
    }

    private static void addPostToCollection(PostCollection postCollection, String postAuthorUsername, int postAuthorUserId, PostUpdate postUpdate) {
        if (postUpdate.isDeletion()) {
            postCollection.addDelete(postAuthorUserId, postUpdate.getId());
        }
        else {
            final Post post = new Post(
                    postAuthorUsername,
                    postAuthorUserId,
                    postUpdate.getId(),
                    postUpdate.getTimestamp(),
                    postUpdate.getContent(),
                    new ArrayList<>());
            postCollection.addPost(post);
        }
    }

    private static void addPostsToContentProvider(Context context, PostCollection postCollection) {
        for (Post post : postCollection.getPosts()) {
            //--TODO use batch insert
            ContentValues values = new ContentValues();
            values.put(IslndContract.PostEntry.COLUMN_USER_ID, post.getUserId());
            values.put(IslndContract.PostEntry.COLUMN_POST_ID, post.getPostId());
            values.put(IslndContract.PostEntry.COLUMN_CONTENT, post.getContent());
            values.put(IslndContract.PostEntry.COLUMN_TIMESTAMP, post.getTimestamp());
            context.getContentResolver().insert(
                    IslndContract.PostEntry.CONTENT_URI,
                    values
            );
        }

        for (PostKey postKey : postCollection.getDeletedKeys()) {
            DataUtils.deletePost(context, postKey);
        }
    }

    public static void post(Context context, PostUpdate postUpdate) {
        EncryptedPost encryptedPost = new EncryptedPost(
                postUpdate,
                Util.getPrivateKey(context),
                Util.getGroupKey(context));

        Rest.post(io.islnd.android.islnd.app.util.Util.getPseudonymSeed(context), encryptedPost, Util.getApiKey(context));
    }

    public static void comment(Context context, int postUserId, String postId, String content) {
        String postAuthorPseudonym = DataUtils.getPseudonym(context, postUserId);
        String myPseudonym = io.islnd.android.islnd.app.util.Util.getPseudonym(context);
        int myId = DataUtils.getUserId(context, io.islnd.android.islnd.app.util.Util.getUser(context));
        CommentUpdate commentUpdate = VersionedContentBuilder.buildComment(
                context,
                postAuthorPseudonym,
                myPseudonym,
                postId,
                content);

        ContentValues values = new ContentValues();
        values.put(IslndContract.CommentEntry.COLUMN_POST_USER_ID, postUserId);
        values.put(IslndContract.CommentEntry.COLUMN_POST_ID, postId);
        values.put(IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID, myId);
        values.put(IslndContract.CommentEntry.COLUMN_COMMENT_ID, commentUpdate.getCommentId());
        values.put(IslndContract.CommentEntry.COLUMN_TIMESTAMP, commentUpdate.getTimestamp());
        values.put(IslndContract.CommentEntry.COLUMN_CONTENT, commentUpdate.getContent());

        context.getContentResolver().insert(
                IslndContract.CommentEntry.CONTENT_URI,
                values);

        Key postAuthorGroupKey = DataUtils.getGroupKey(context, postUserId);
        EncryptedComment encryptedComment = new EncryptedComment(
                commentUpdate,
                Util.getPrivateKey(context),
                postAuthorGroupKey,
                postAuthorPseudonym,
                commentUpdate.getPostId());

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Rest.postComment(
                        encryptedComment,
                        io.islnd.android.islnd.app.util.Util.getApiKey(context));
                return null;
            }
        }.execute();
    }

    public static void postProfile(Context context, ProfileWithImageData profile) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String privateKey = preferences.getString(context.getString(R.string.private_key), "");
        String myGroupKey = preferences.getString(context.getString(R.string.group_key), "");

        EncryptedProfile profilePost = new EncryptedProfile(
                profile,
                CryptoUtil.decodePrivateKey(privateKey),
                CryptoUtil.decodeSymmetricKey(myGroupKey));

        String pseudonymSeed = preferences.getString(context.getString(R.string.pseudonym_seed), "");
        Rest.postProfile(pseudonymSeed, profilePost, Util.getApiKey(context));
    }

    public static String getPseudonym(Context context, String seed) {
        return Rest.getPseudonym(seed, Util.getApiKey(context));
    }

    public static boolean addFriendFromEncodedIdentityString(Context context,
                                                             String encodedString) {
        Log.v(TAG, "adding friend from encoded string: " + encodedString);
        byte[] bytes = new Decoder().decode(encodedString);
        PseudonymKey pk = PseudonymKey.fromProto(bytes);
        return addFriendToDatabaseAndCreateDefaultProfile(context, pk);
    }

    private static boolean addFriendToDatabaseAndCreateDefaultProfile(Context context, PseudonymKey pk) {
        //--TODO only add if not already friends
        DataUtils.insertUser(context, pk);

        Profile profile = io.islnd.android.islnd.messaging.Util.buildDefaultProfile(context, pk.getUsername());
        ProfileDatabase.getInstance(context).insert(profile);

        return true;
    }

    public static String getEncodedIdentityString(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long uniqueId = sharedPreferences.getLong(context.getString(R.string.pseudonym_key_id), 0);
        String username = sharedPreferences.getString(context.getString(R.string.user_name), "");
        String pseudonym = sharedPreferences.getString(context.getString(R.string.pseudonym), "");
        Log.v(TAG, String.format("pseudonym is %s", pseudonym));
        Key groupKey = CryptoUtil.decodeSymmetricKey(
                sharedPreferences.getString(context.getString(R.string.group_key), ""));

        PseudonymKey pk = new PseudonymKey(uniqueId, username, pseudonym, groupKey);
        String encodeString = new Encoder().encodeToString(pk.toByteArray());
        Log.v(TAG, "generated encoded string: " + encodeString);
        return encodeString;
    }

    public static ProfileWithImageData getMostRecentProfile(Context context, String username) {
//        PseudonymKey friendPK = FriendDatabase.getInstance(context).getKey(username);
//        String apiKey = Util.getApiKey(context);
//        List<EncryptedProfile> encryptedProfiles = Rest.getProfiles(friendPK.getPseudonym(), apiKey);
//        if (encryptedProfiles == null) {
//            Log.d(TAG, "profile response was null");
//            return null;
//        }
//
//        List<ProfileWithImageData> profiles = new ArrayList<>();
//        for (EncryptedProfile encryptedProfile : encryptedProfiles) {
//            //--TODO check signature
//            profiles.add(encryptedProfile.decrypt(friendPK.getKey()));
//        }
//
//        return Util.getNewest(profiles);
        return new ProfileWithImageData(
                "default",
                "default",
                null,
                null,
                0
        );
    }

    public static CommentCollection getCommentCollection(Context context, int postAuthorId, String postId) {
        Log.v(TAG, String.format("getting comments user id %d post id %s", postAuthorId, postId));
        List<CommentQuery> queries = new ArrayList<>();
        String postAuthorPseudonym = DataUtils.getPseudonym(context, postAuthorId);
        queries.add(new CommentQuery(postAuthorPseudonym, postId));
        return getCommentCollection(context, queries, postAuthorId);
    }

    public static CommentCollection getCommentCollection(
            Context context,
            List<CommentQuery> queries,
            int postAuthorId) {
        CommentCollection commentCollection= new CommentCollection();
        CommentQueryRequest commentQueryPost = new CommentQueryRequest(queries);

        List<EncryptedComment> encryptedComments = Rest.getComments(
                commentQueryPost,
                io.islnd.android.islnd.app.util.Util.getApiKey(context));
        if (encryptedComments == null) {
            return new CommentCollection();
        }

        for (EncryptedComment ec : encryptedComments) {

            CommentUpdate commentUpdate = ec.decrypt(DataUtils.getGroupKey(context, postAuthorId));
            final String commentAuthorPseudonym = commentUpdate.getCommentAuthorPseudonym();
            String commentAuthorUsername = DataUtils.getUsernameFromPseudonym(
                    context,
                    commentAuthorPseudonym);

            if (commentAuthorUsername == null) {
                Log.d(TAG, "could not find a username for pseudonym " + commentAuthorPseudonym);
                throw new UnsupportedOperationException("Friend relations must be symmetric!");
            }

            int commentAuthorId = DataUtils.getUserId(context, commentAuthorUsername);
            if (commentUpdate.isDeletion()
                    && commentAuthorId == -1) {
                Log.v(TAG, "adding commment for unknown user");
            }

            commentCollection.add(postAuthorId, commentAuthorId, commentUpdate);
        }

        return commentCollection;
    }

    public static long getServerTimeOffsetMillis(Context context, int repetitions) throws IOException {
        return Rest.getServerTimeOffsetMillis(repetitions, Util.getApiKey(context));
    }
}
