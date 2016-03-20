package io.islnd.android.islnd.app.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.models.CommentKey;
import io.islnd.android.islnd.app.models.Post;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.util.Util;

import io.islnd.android.islnd.messaging.CommentUpdate;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.ServerTime;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.MessageLayer;
import io.islnd.android.islnd.messaging.crypto.EncryptedComment;

import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.SecureRandom;

public class IslndDb
{
    private static final String TAG = "IslndDb";

    public static void createIdentity(Context context, String displayName) {
        setKeyPairAndPostPublicKey(context);
        setGroupKey(context);

        //--TODO passing display name here is a hack
        //  We will be moving create identity stuff into a service
        //  instead of nested async tasks
        setPseudonym(context, displayName);
    }

    private static void setPseudonym(Context context, String displayName) {
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
                        displayName,
                        pseudonym,
                        Util.getGroupKey(context),
                        Util.getPublicKey(context));

                editor.putString(context.getString(R.string.alias), pseudonym);
                editor.putInt(context.getString(R.string.user_id), (int)userId);
                editor.commit();

                Profile defaultProfile = Util.buildDefaultProfile(
                        context,
                        displayName);
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

    public static void syncServerTime(Context context, boolean force) {
        final String prefKey = context.getString(R.string.server_time_offset);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String savedOffset = settings.getString(prefKey, null);

        if (force || savedOffset == null) {
            // get from server
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        long serverTimeOffset = ServerTime.synchronize(context);
                        settings.edit()
                                .putString(prefKey, Long.toString(serverTimeOffset))
                                .apply();
                        Log.d(TAG, "ServerTime: saved new offset to prefs: " + serverTimeOffset);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        } else {
            ServerTime.synchronizeManually(Long.parseLong(savedOffset));
            Log.d(TAG, "ServerTime: loaded offset from prefs: " + savedOffset);
        }
    }
}