package com.island.island.Database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.island.island.Models.Post;
import com.island.island.Models.Profile;
import com.island.island.Models.ProfileWithImageData;
import com.island.island.R;
import com.island.island.Utils.Utils;
import com.island.island.VersionedContentBuilder;

import org.island.messaging.PostUpdate;
import org.island.messaging.Util;
import org.island.messaging.crypto.CryptoUtil;
import org.island.messaging.MessageLayer;

import java.security.KeyPair;
import java.security.SecureRandom;

/**
 * Created by David Thompson on 12/20/2015.
 *
 * This class holds the functions to talk to the database.
 */
public class IslandDB
{
    private static final String TAG = "IslandDB";

    // Used json minifier online
    public static String mockData = "{\"users\":[\"Bill Gates\",\"Steve Jobs\",\"Fred Flintstone\",\"John Smith\",\"Thom Yorke\"],\"Bill Gates\":{\"posts\":{\"0\":{\"timestamp\":\"1453837198\",\"content\":\"I created Microsoft! I've got billions and dollars and I donate most of it to charity. Windows 10 is awesome. Vista was horrible.\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"},{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]},\"1\":{\"timestamp\":\"1253837198\",\"content\":\"I made a bunch of money today!\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"},{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]},\"2\":{\"timestamp\":\"1253937198\",\"content\":\"I made even more money today!\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"},{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]}},\"profile\":{\"about_me\":\"Founder of Microsoft.\"}},\"Steve Jobs\":{\"posts\":{\"0\":{\"timestamp\":\"1453837000\",\"content\":\"*** British Voice *** Steve Jobs was cutting edge. He changed computing as we know it. His innovations are more important that you.\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]},\"1\":{\"timestamp\":\"1213857000\",\"content\":\"I'm innovative lol :)\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]}},\"profile\":{\"about_me\":\"Founder of Apple.\"}},\"Fred Flintstone\":{\"posts\":{\"0\":{\"timestamp\":\"1453830198\",\"content\":\"I am prehistoric. I know the guy that invented the wheel! My best friend is Barney. My pet dinosaur always tricks me.\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"},{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]},\"1\":{\"timestamp\":\"1400830198\",\"content\":\"Watch my show tonight!\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]}},\"profile\":{\"about_me\":\"Citizen of Bedrock.\"}},\"John Smith\":{\"posts\":{\"0\":{\"timestamp\":\"1453807198\",\"content\":\"This post is boring just like my name :)\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]}},\"profile\":{\"about_me\":\"I ain't nothin'.\"}},\"Thom Yorke\":{\"posts\":{\"0\":{\"timestamp\":\"1450837198\",\"content\":\"I am radiohead lol. I have a high voice but it's cool because I'm nasty with a synth. Get at me Damon Albarn. Gorillaz suk lol ayyyyyyeeeee. ISLAND NEEDS TO SUPPORT EMOJIS AYYYYEEEEe.\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]}},\"profile\":{\"about_me\":\"I'm in that one band.\"}}}";

    public static void postPublicKey(Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = preferences.getString(context.getString(R.string.user_name), "");
        String publicKey = preferences.getString(context.getString(R.string.public_key), "");
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                MessageLayer.postPublicKey(context, username, CryptoUtil.decodePublicKey(publicKey));
                Log.v(TAG, "post key completed");
                return new Object();
            }
        }.execute();
    }

    public static void createIdentity(Context context, String username) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String currentUsername = settings.getString(context.getString(R.string.user_name), "");
        Log.v(TAG, String.format("previous user %s, current user %s", currentUsername, username));
        if (currentUsername.equals(username)) {
            //--The app is already using this user
            return;
        }

        setUsername(context, username);
        setKeyPairAndPostPublicKey(context);
        setGroupKey(context);
        setPseudonym(context);

        //--Add a default profile
        ProfileDatabase profileDatabase = ProfileDatabase.getInstance(context);
        profileDatabase.insert(Util.buildDefaultProfile(context, username));
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
                editor.putString(context.getString(R.string.pseudonym), pseudonym);
                editor.commit();

                Log.v(TAG, "pseudonym " + pseudonym);
                Log.v(TAG, "pseudonym seed " + seed);

                DataUtils.insertUser(
                        context,
                        Utils.getUser(context),
                        Utils.getPseudonym(context),
                        Utils.getGroupKey(context));
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

    private static void setUsername(Context context, String username) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(context.getString(R.string.user_name), username);
        editor.commit();
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

            @Override
            protected void onPostExecute(Void aVoid) {
                IslandDB.postPublicKey(context);
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
        int myUserId = DataUtils.getUserId(context, Utils.getUser(context));
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

    public static void removeReader(String username)
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

    public static Profile getProfile(Context context, String username) {
        return ProfileDatabase.getInstance(context).get(username);
    }

    public static Profile getMostRecentProfile(Context context, String username) {
        Profile profile;

        if (!Utils.isUser(context, username)) {
            ProfileWithImageData profileWithImageData = MessageLayer.getMostRecentProfile(
                    context,
                    username);
            if (profileWithImageData == null) {
                Log.v(TAG, "no profile on network for " + username);
                return null;
            }

            ProfileDatabase profileDatabase = ProfileDatabase.getInstance(context);
            profile = Utils.saveProfileWithImageData(context, profileWithImageData);

            if (profileDatabase.hasProfile(username)) {
                profileDatabase.update(profile);
            } else {
                profileDatabase.insert(profile);
            }
        } else {
            profile = ProfileDatabase.getInstance(context).get(username);
        }

        return profile;
    }

    public static void deletePost(Context context, int userId, String postId) {
        Log.v(TAG, String.format("deleting post. user %d post %s", userId, postId));
        //--TODO delete the post
//        PostDatabase postDatabase = PostDatabase.getInstance(context);
//        postDatabase.delete(userId, postId);
//        PostUpdate deletePost = PostUpdate.buildDelete(postId);
//        EncryptedPost encryptedPost = new EncryptedPost(
//                deletePost,
//                Utils.getPrivateKey(context),
//                Utils.getGroupKey(context));
//        Rest.post(Utils.getPseudonymSeed(context), encryptedPost, Utils.getApiKey(context));
    }

    public static void deleteComment(
            Context context,
            int postUserId,
            String postId,
            int commentUserId,
            String commentId) {
//        FriendDatabase friendDatabase = FriendDatabase.getInstance(context);
//        PseudonymKey postAuthorPseudonymKey = friendDatabase.getKey(postUserId);
//        String commentAuthorPseudonym = friendDatabase.getPseudonym(commentUserId);
//        CommentUpdate deleteComment = CommentUpdate.buildDelete(
//                postAuthorPseudonymKey.getPseudonym(),
//                commentAuthorPseudonym,
//                postId,
//                commentId);
//
//        EncryptedComment encryptedComment = new EncryptedComment(
//                deleteComment,
//                Utils.getPrivateKey(context),
//                postAuthorPseudonymKey.getKey(),
//                postAuthorPseudonymKey.getPseudonym(),
//                postId);
//        Rest.postComment(encryptedComment, Utils.getApiKey(context));
    }
}