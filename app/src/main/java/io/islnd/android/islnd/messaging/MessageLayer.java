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
import io.islnd.android.islnd.messaging.crypto.InvalidSignatureException;
import io.islnd.android.islnd.messaging.server.CommentQuery;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.Post;
import io.islnd.android.islnd.app.models.PostKey;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.models.ProfileWithImageData;
import io.islnd.android.islnd.app.models.User;
import io.islnd.android.islnd.app.PostCollection;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.app.VersionedContentBuilder;

import io.islnd.android.islnd.messaging.crypto.EncryptedProfile;
import io.islnd.android.islnd.messaging.server.CommentQueryRequest;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class MessageLayer {
    private static final String TAG = MessageLayer.class.getSimpleName();

    public static PostCollection getPosts(Context context) {

        List<Identity> keys = getPseudonymKeys(context);
        PostCollection postCollection = new PostCollection();
        String apiKey = Util.getApiKey(context);

        for (Identity friendIdentity : keys) {
            List<EncryptedPost> encryptedPosts = Rest.getPosts(
                    friendIdentity.getAlias(),
                    apiKey);
            if (encryptedPosts == null) {
                continue;
            }

            int friendUserId = DataUtils.getUserIdFromPublicKey(
                    context,
                    friendIdentity.getPublicKey());
            for (EncryptedPost encryptedPost: encryptedPosts) {
                PostUpdate postUpdate = null;
                try {
                    postUpdate = encryptedPost.decryptAndVerify(friendIdentity.getGroupKey(), friendIdentity.getPublicKey());
                } catch (InvalidSignatureException e) {
                    Log.d(TAG, "could not verify post " + postUpdate);
                    e.printStackTrace();
                }
                if (postUpdate == null) {
                    continue;
                }

                //--TODO check that post is signed
                addPostToCollection(
                        postCollection,
                        friendIdentity.getDisplayName(),
                        friendUserId,
                        postUpdate);
            }
        }

        addPostsToContentProvider(context, postCollection);
        return postCollection;
    }

    private static List<Identity> getPseudonymKeys(Context context) {
        String[] projection = new String[] {
                IslndContract.UserEntry.TABLE_NAME + "." + IslndContract.UserEntry._ID,
                IslndContract.UserEntry.COLUMN_PUBLIC_KEY,
                IslndContract.AliasEntry.COLUMN_ALIAS,
                IslndContract.AliasEntry.COLUMN_GROUP_KEY,
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.IdentityEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        List<Identity> identities = new ArrayList<>();
        if (!cursor.moveToFirst()) {
            return identities;
        }

        do {
            String alias = cursor.getString(cursor.getColumnIndex(IslndContract.AliasEntry.COLUMN_ALIAS));
            Key groupKey = CryptoUtil.decodeSymmetricKey(
                    cursor.getString(cursor.getColumnIndex(IslndContract.AliasEntry.COLUMN_GROUP_KEY)));
            Key publicKey = CryptoUtil.decodePublicKey(
                    cursor.getString(cursor.getColumnIndex(IslndContract.UserEntry.COLUMN_PUBLIC_KEY)));
            String displayName =
                    cursor.getString(cursor.getColumnIndex(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME));
            identities.add(new Identity(
                            displayName,
                            alias,
                            groupKey,
                            publicKey));
        } while (cursor.moveToNext());

        return identities;
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
        String postAuthorPseudonym = DataUtils.getMostRecentAlias(context, postUserId);
        String myPseudonym = io.islnd.android.islnd.app.util.Util.getAlias(context);
        int myId = Util.getUserId(context);
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
        Identity pk = Identity.fromProto(bytes);
        return addFriendToDatabaseAndCreateDefaultProfile(context, pk);
    }

    private static boolean addFriendToDatabaseAndCreateDefaultProfile(Context context, Identity pk) {
        //--TODO only add if not already friends
        long userId = DataUtils.insertUser(context, pk);

        Profile profile = Util.buildDefaultProfile(context, pk.getDisplayName());
        DataUtils.insertProfile(context, profile, userId);

        return true;
    }

    public static String getEncodedIdentityString(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long uniqueId = sharedPreferences.getLong(context.getString(R.string.pseudonym_key_id), 0);
        String displayName = sharedPreferences.getString(context.getString(R.string.display_name), "");
        String alias = sharedPreferences.getString(context.getString(R.string.alias), "");
        Log.v(TAG, String.format("alias is %s", alias));
        Key groupKey = CryptoUtil.decodeSymmetricKey(
                sharedPreferences.getString(context.getString(R.string.group_key), ""));
        Key publicKey = CryptoUtil.decodePublicKey(
                sharedPreferences.getString(context.getString(R.string.public_key), ""));

        Identity pk = new Identity(displayName, alias, groupKey, publicKey);
        String encodeString = new Encoder().encodeToString(pk.toByteArray());
        Log.v(TAG, "generated encoded string: " + encodeString);
        return encodeString;
    }

    public static ProfileWithImageData getMostRecentProfile(Context context, int userId) {
        String pseudonym = DataUtils.getMostRecentAlias(context, userId);
        Key groupKey = DataUtils.getGroupKey(context, userId);
        Key publicKey = DataUtils.getPublicKey(context, userId);

        String apiKey = Util.getApiKey(context);
        List<EncryptedProfile> encryptedProfiles = Rest.getProfiles(pseudonym, apiKey);
        if (encryptedProfiles == null) {
            Log.d(TAG, "profile response was null");
            return null;
        }

        List<ProfileWithImageData> profiles = new ArrayList<>();
        for (EncryptedProfile encryptedProfile : encryptedProfiles) {
            try {
                profiles.add(encryptedProfile.decryptAndVerify(groupKey, publicKey));
            } catch (InvalidSignatureException e) {
                Log.d(TAG, "could not verify profile for user id " + userId);
            }
        }

        return io.islnd.android.islnd.messaging.Util.getNewest(profiles);
    }

    public static CommentCollection getCommentCollection(Context context, int postAuthorId, String postId) {
        Log.v(TAG, String.format("getting comments user id %d post id %s", postAuthorId, postId));
        List<CommentQuery> queries = new ArrayList<>();
        String postAuthorPseudonym = DataUtils.getMostRecentAlias(context, postAuthorId);
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

        Log.v(TAG, encryptedComments.size() + " comments");
        for (EncryptedComment ec : encryptedComments) {
            final Key groupKey = DataUtils.getGroupKey(context, postAuthorId);
            CommentUpdate commentUpdate = ec.decrypt(groupKey);
            try {
                final int commentAuthorId = DataUtils.getUserIdFromAlias(context, commentUpdate.getCommentAuthorPseudonym());
                final Key publicKey = DataUtils.getPublicKey(context, commentAuthorId);
                ec.decryptAndVerify(groupKey, publicKey);
            } catch (InvalidSignatureException e) {
                Log.d(TAG, "could not verify comment: " + commentUpdate);
                e.printStackTrace();
            }
            final String commentAuthorPseudonym = commentUpdate.getCommentAuthorPseudonym();

            int commentAuthorId = DataUtils.getUserIdFromAlias(context, commentAuthorPseudonym);
            if (commentUpdate.isDeletion()
                    && commentAuthorId == -1) {
                Log.v(TAG, "adding commment for unknown user");
            }

            commentCollection.add(postAuthorId, commentAuthorId, commentUpdate);
        }

        return commentCollection;
    }
}
