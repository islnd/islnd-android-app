package org.island.messaging;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.island.island.Database.CommentDatabase;
import com.island.island.Database.DataUtils;
import com.island.island.Database.IslndContract;
import com.island.island.Database.ProfileDatabase;
import com.island.island.Models.CommentKey;
import com.island.island.Models.Post;
import com.island.island.Models.PostKey;
import com.island.island.Models.Profile;
import com.island.island.Models.ProfileWithImageData;
import com.island.island.Models.Comment;
import com.island.island.Models.User;
import com.island.island.PostCollection;
import com.island.island.R;
import com.island.island.Utils.Utils;

import org.island.messaging.crypto.CryptoUtil;
import org.island.messaging.crypto.EncryptedComment;
import org.island.messaging.crypto.EncryptedData;
import org.island.messaging.crypto.EncryptedPost;
import org.island.messaging.crypto.EncryptedProfile;
import org.island.messaging.crypto.ObjectEncrypter;
import org.island.messaging.server.CommentQuery;
import org.island.messaging.server.CommentQueryRequest;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageLayer {
    private static final String TAG = MessageLayer.class.getSimpleName();
    private static final String UNKNOWN_USER_NAME = "<UNKNOWN USER NAME>";

    public static List<User> getReaders(Context context, String username, Key privateKey) {
        //call the REST service
        List<EncryptedData> keys = Rest.getReaders(username, Utils.getApiKey(context));
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
        Rest.postPublicKey(username, CryptoUtil.encodeKey(publicKey), Utils.getApiKey(context));
    }

    public static PostCollection getPosts(Context context) {

        List<PseudonymKey> keys = getPseudonymKeys(context);
        PostCollection postCollection = new PostCollection();
        String apiKey = Utils.getApiKey(context);

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
            //--TODO only insert if not in DB
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
            //--TODO handle deletes
        }
    }

    public static void post(Context context, PostUpdate postUpdate) {
        EncryptedPost encryptedPost = new EncryptedPost(
                postUpdate,
                Utils.getPrivateKey(context),
                Utils.getGroupKey(context));

        Rest.post(Utils.getPseudonymSeed(context), encryptedPost, Utils.getApiKey(context));
    }

    public static Comment comment(Context context, int postUserId, String postId, String content) {
//        FriendDatabase friendDatabase = FriendDatabase.getInstance(context);
//        int commentUserId = friendDatabase.getUserId(Utils.getUser(context));
//        String postAuthorPseudonym = friendDatabase.getPseudonym(postUserId); // this will be pseudonym database
//        String myPseudonym = Utils.getPseudonym(context);
//        CommentUpdate commentUpdate = VersionedContentBuilder.buildComment(
//                context,
//                postAuthorPseudonym,
//                myPseudonym,
//                postId,
//                content);
//
//        CommentDatabase.getInstance(context).insert(commentUserId, postUserId, commentUpdate);
//
//        PseudonymKey postAuthorGroupKey = friendDatabase.getKey(postUserId);
//        EncryptedComment encryptedComment = new EncryptedComment(
//                commentUpdate,
//                Utils.getPrivateKey(context),
//                postAuthorGroupKey.getKey(),
//                postAuthorPseudonym,
//                commentUpdate.getPostId());
//
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                Rest.postComment(
//                        encryptedComment,
//                        Utils.getApiKey(context));
//                return null;
//            }
//        }.execute();
//
//        return new Comment(
//                postUserId,
//                postId,
//                commentUserId,
//                commentUpdate.getCommentId(),
//                content,
//                commentUpdate.getTimestamp());
        return null;
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
        Rest.postProfile(pseudonymSeed, profilePost, Utils.getApiKey(context));
    }

    public static String getPseudonym(Context context, String seed) {
        return Rest.getPseudonym(seed, Utils.getApiKey(context));
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

        Profile profile = Util.buildDefaultProfile(context, pk.getUsername());
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
//        String apiKey = Utils.getApiKey(context);
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
        return getCommentCollection(context, queries);
    }

    public static CommentCollection getCommentCollection(Context context, List<CommentQuery> queries) {
        CommentCollection commentCollection= new CommentCollection();
//        CommentQueryRequest commentQueryPost = new CommentQueryRequest(queries);
//
//        List<EncryptedComment> encryptedComments = Rest.getComments(
//                commentQueryPost,
//                Utils.getApiKey(context));
//        if (encryptedComments == null) {
//            return new CommentCollection();
//        }
//
//        FriendDatabase friendDatabase = FriendDatabase.getInstance(context);
//        CommentDatabase commentDatabase = CommentDatabase.getInstance(context);
//
//        for (EncryptedComment ec : encryptedComments) {
//            PseudonymKey postAuthorPseudonymKey = friendDatabase.getKey(
//                    friendDatabase.getUsernameFromPseudonym(ec.getPostAuthorPseudonym()));
//
//            CommentUpdate commentUpdate = ec.decrypt(postAuthorPseudonymKey.getKey());
//            String commentAuthorUsername =
//                    friendDatabase.getUsernameFromPseudonym(commentUpdate.getCommentAuthorPseudonym());
//
//            if (commentAuthorUsername == null) {
//                commentAuthorUsername = UNKNOWN_USER_NAME;
//            }
//
//            int postAuthorId = friendDatabase.getUserId(postAuthorPseudonymKey.getUsername());
//            int commentAuthorId = friendDatabase.getUserId(commentAuthorUsername);
//
//            if (commentUpdate.isDeletion()
//                    && commentAuthorId == -1) {
//                Log.v(TAG, "adding commment for unknown user");
//            }
//
//            commentCollection.add(postAuthorId, commentAuthorId, commentUpdate);
//        }
//
//        Map<PostKey, List<Comment>> postKeyToComments = commentCollection.getCommentsGroupedByPostKey();
//        for (PostKey postKey : postKeyToComments.keySet()) {
//            List<Comment> comments = postKeyToComments.get(postKey);
//            for (Comment comment : comments) {
//                if (!commentDatabase.contains(comment)) {
//                    commentDatabase.insert(comment, postKey.getUserId(), postKey.getPostId());
//                }
//            }
//        }
//
//        Map<PostKey, List<CommentKey>> postKeyToDeletions = commentCollection.getDeletions();
//        for (PostKey postKey : postKeyToDeletions.keySet()) {
//            List<CommentKey> deletions = postKeyToDeletions.get(postKey);
//            for (CommentKey keyToDelete : deletions) {
//                if (commentDatabase.contains(keyToDelete)) {
//                    commentDatabase.delete(keyToDelete);
//                }
//            }
//        }

        return commentCollection;
    }
}
