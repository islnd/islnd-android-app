package io.islnd.android.islnd.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.models.CommentKey;
import io.islnd.android.islnd.app.models.Post;
import io.islnd.android.islnd.app.models.PostKey;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.models.ProfileWithImageData;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.app.VersionedContentBuilder;

import io.islnd.android.islnd.messaging.CommentUpdate;
import io.islnd.android.islnd.messaging.PostUpdate;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.MessageLayer;
import io.islnd.android.islnd.messaging.crypto.EncryptedComment;
import io.islnd.android.islnd.messaging.crypto.EncryptedPost;

import java.security.Key;
import java.security.KeyPair;
import java.security.SecureRandom;

public class IslndDb
{
    private static final String TAG = "IslndDb";

    public static void createIdentity(Context context, String displayName) {
        Util.setDisplayName(context, displayName);
        setKeyPairAndPostPublicKey(context);
        setGroupKey(context);
        setPseudonym(context);
    }

    private static void setPseudonym(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        String seed = String.valueOf(new SecureRandom().nextLong());
        editor.putString(context.getString(R.string.pseudonym_seed), seed);

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                return MessageLayer.getPseudonym(context, params[0]);
            }

            @Override
            protected void onPostExecute(String pseudonym) {

                Log.v(TAG, "pseudonym " + pseudonym);
                Log.v(TAG, "pseudonym seed " + seed);

                long userId = DataUtils.insertUser(
                        context,
                        Util.getDisplayName(context),
                        pseudonym,
                        Util.getGroupKey(context),
                        Util.getPublicKey(context));

                editor.putString(context.getString(R.string.alias), pseudonym);
                editor.putInt(context.getString(R.string.user_id), (int)userId);
                editor.commit();

                Profile defaultProfile = Util.buildDefaultProfile(
                        context,
                        Util.getDisplayName(context));
                DataUtils.insertProfile(
                        context,
                        defaultProfile,
                        userId);
            }
        }.execute(seed);
    }

    private static void setGroupKey(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        String groupKey = CryptoUtil.encodeKey(CryptoUtil.getKey());
        editor.putString(context.getString(R.string.group_key), groupKey);
        editor.commit();

        Log.v(TAG, "group key " + groupKey);
    }

    private static void setKeyPairAndPostPublicKey(Context context) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();

                KeyPair keyPair = CryptoUtil.getKeyPair();
                String privateKey = CryptoUtil.encodeKey(keyPair.getPrivate());
                String publicKey = CryptoUtil.encodeKey(keyPair.getPublic());
                editor.putString(context.getString(R.string.private_key), privateKey);
                editor.putString(context.getString(R.string.public_key), publicKey);
                editor.commit();

                Log.v(TAG, "private key " + privateKey);
                Log.v(TAG, "public key " + publicKey);

                return null;
            }
        }.execute();
    }

    public static PostUpdate post(Context context, String content)
    /**
     * Encrypts content and posts to database.
     *
     * @param content Plaintext content to be posted.
     */
    {
        PostUpdate postUpdate = VersionedContentBuilder.buildPost(context, content);
        int myUserId = Util.getUserId(context);
        ContentValues values = new ContentValues();
        values.put(IslndContract.PostEntry.COLUMN_USER_ID, myUserId);
        values.put(IslndContract.PostEntry.COLUMN_POST_ID, postUpdate.getId());
        values.put(IslndContract.PostEntry.COLUMN_CONTENT, postUpdate.getContent());
        values.put(IslndContract.PostEntry.COLUMN_TIMESTAMP, postUpdate.getTimestamp());
        context.getContentResolver().insert(
                IslndContract.PostEntry.CONTENT_URI,
                values
        );

        Log.v(TAG, String.format("making post user id %d post id %s", myUserId, postUpdate.getId()));

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                MessageLayer.post(context, postUpdate);
                return null;
            }
        }.execute();

        //--TODO we might be able to remove this
        return postUpdate;
    }

    public static void allowReader(String username)
    /**
     * Post my encrypted pseudonym and group key with the reader's public key.
     *
     * @param username Username of user I am allowing to read my posts.
     */
    {

    }

    public static void removeReader(int userId)
    /**
     * Removes user by changing my pseudonym, changing my groupKey, and allowing all users I want to
     * keep.
     *
     * @param username Username of user I want to remove.
     */
    {

    }

    public static void changePseudonym(String pseudonym)
    /**
     * Changes my pseudonym and adds all my friends again.
     *
     * @param pseudonym New pseudonym.
     */
    {

    }

    public static void addCommentToPost(Context context, Post post, String commentText)
    /**
     * Adds comment to existing post
     *
     * @param post Post I am adding comment to.
     * @param comment Comment that I'm adding.
     */
    {
        MessageLayer.comment(
                context,
                post.getUserId(),
                post.getPostId(),
                commentText);
    }

    public static void postProfile(Context context, ProfileWithImageData profile) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                MessageLayer.postProfile(context, profile);
                Log.v(TAG, "profile posted to server");
                return null;
            }
        }.execute();
    }

    public static Profile getMostRecentProfile(Context context, int userId) {
        Profile profile;

        if (!Util.isUser(context, userId)) {
            ProfileWithImageData profileWithImageData = MessageLayer.getMostRecentProfile(
                    context,
                    userId);
            if (profileWithImageData == null) {
                Log.v(TAG, "no profile on network for user " + userId);
                return null;
            }

            profile = Util.saveProfileWithImageData(context, profileWithImageData);
            DataUtils.insertProfile(context, profile, userId);

        } else {
            profile = DataUtils.getProfile(context, userId);
        }

        return profile;
    }

    public static void deletePost(Context context, int userId, String postId) {
        Log.v(TAG, String.format("deleting post. user %d post %s", userId, postId));
        DataUtils.deletePost(context, new PostKey(userId, postId));
        PostUpdate deletePost = PostUpdate.buildDelete(postId);
        EncryptedPost encryptedPost = new EncryptedPost(
                deletePost,
                Util.getPrivateKey(context),
                Util.getGroupKey(context));
        Rest.post(Util.getPseudonymSeed(context), encryptedPost, Util.getApiKey(context));
    }

    public static void deleteComment(
            Context context,
            int postUserId,
            String postId,
            int commentUserId,
            String commentId) {
        String postAuthorPseudonym = DataUtils.getMostRecentAlias(context, postUserId);
        String commentAuthorPseudonym = DataUtils.getMostRecentAlias(context, commentUserId);
        Key postAuthorGroupKey = DataUtils.getGroupKey(context, postUserId);

        CommentUpdate deleteComment = CommentUpdate.buildDelete(
                postAuthorPseudonym,
                commentAuthorPseudonym,
                postId,
                commentId);

        EncryptedComment encryptedComment = new EncryptedComment(
                deleteComment,
                Util.getPrivateKey(context),
                postAuthorGroupKey,
                postAuthorPseudonym,
                postId);

        // Delete local
        DataUtils.deleteComment(
                context,
                new CommentKey(commentUserId, commentId));

        // Delete from network
        Rest.postComment(encryptedComment, Util.getApiKey(context));
    }
}