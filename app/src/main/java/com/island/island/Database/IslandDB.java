package com.island.island.Database;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.island.island.Models.Comment;
import com.island.island.Models.Post;
import com.island.island.Models.Profile;
import com.island.island.Models.User;
import com.island.island.R;
import com.island.island.Utils.Utils;

import org.island.messaging.Crypto;
import org.island.messaging.MessageLayer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private static IdentityDatabase IDENTITY_DB;

    public static void postPublicKey(Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = preferences.getString(context.getString(R.string.user_name), "");
        String publicKey = preferences.getString(context.getString(R.string.public_key), "");
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                Log.v(TAG, "starting post key");
                Log.v(TAG, "username: " + username);
                Log.v(TAG, "public key: " + publicKey);
                MessageLayer.postPublicKey(username, Crypto.decodePublicKey(publicKey));
                Log.v(TAG, "post key completed");
                return new Object();
            }
        }.execute();
    }

    @NonNull
    private static IdentityDatabase getIdentityDatabase(Context context) {
        if (IDENTITY_DB == null) {
            IDENTITY_DB = new IdentityDatabase(context);
        }

        return IDENTITY_DB;
    }

    public static void createIdentity(Context context, String username)
    {
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
    }

    private static void setPseudonym(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        String seed = String.valueOf(new SecureRandom().nextLong());
        editor.putString(context.getString(R.string.pseudonym_seed), seed);

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                return MessageLayer.getPseudonym(params[0]);
            }

            @Override
            protected void onPostExecute(String pseudonym) {
                editor.putString(context.getString(R.string.pseudonym), pseudonym);
                editor.commit();

                Log.v(TAG, "pseudonym " + pseudonym);
                Log.v(TAG, "pseudonym seed " + seed);
            }
        }.execute(seed);
    }

    private static void setGroupKey(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        String groupKey = Crypto.encodeKey(Crypto.getKey());
        editor.putString(context.getString(R.string.group_key), groupKey);
        editor.commit();

        Log.v(TAG, "group key " + groupKey);
    }

    private static void setUsername(Context context, String username) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(context.getString(R.string.user_name), username);
        editor.commit();

        Log.v(TAG, "username " + username);
    }

    private static void setKeyPairAndPostPublicKey(Context context) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();

                KeyPair keyPair = Crypto.getKeyPair();
                String privateKey = Crypto.encodeKey(keyPair.getPrivate());
                String publicKey = Crypto.encodeKey(keyPair.getPublic());
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

    public static void post(Context context, String content)
    /**
     * Encrypts content and posts to database.
     *
     * @param content Plaintext content to be posted.
     */
    {
        new AsyncTask<String, Void, Void>() {

            @Override
            protected Void doInBackground(String... params) {
                MessageLayer.post(context, params[0]);
                return null;
            }

        }.execute(content);
    }

    public static List<Post> getPostsForUser(User user)
    /**
     * Gets decrypted posts from user.
     *
     * @param user User whose posts I'm getting.
     * @return The designated user's posts.
     */
    {
        // Get encrypted posts
        // Decrypt posts
        // Return

        // Return empty list for now
        List<Post> posts = new ArrayList<>();

        try
        {
            JSONObject obj = new JSONObject(mockData);
            JSONObject userPosts = obj.getJSONObject(user.getUserName()).getJSONObject("posts");
            Iterator<?> keys = userPosts.keys();

            while(keys.hasNext())
            {
                String key = (String)keys.next();
                JSONObject postObject = userPosts.getJSONObject(key);
                String timestamp = postObject.getString("timestamp");
                timestamp = Utils.smartTimestampFromUnixTime(Long.parseLong(timestamp));
                String content = postObject.getString("content");

                List<Comment> comments = new ArrayList<>();
                JSONArray commentArray = postObject.getJSONArray("comments");

                for(int i = 0; i < commentArray.length(); ++i)
                {
                    JSONObject commentObj = commentArray.getJSONObject(i);
                    Comment comment = new Comment(commentObj.getString("user"),
                            commentObj.getString("comment"));
                    comments.add(comment);
                }

                posts.add(new Post(user.getUserName(), timestamp, content, comments));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return posts;
    }

    public static List<Post> getLastNPostsForUser(User user, int n)
    /**
     * Gets last n number of decrypted posts from user
     *
     * @param user User whose posts I'm getting.
     * @param n Number of posts I'm getting.
     * @return N number of designated user's posts.
     */
    {
        // Get last n encrypted posts
        // Decrypt posts
        // Return

        // Return empty list for now
        List<Post> posts = new ArrayList<>();

        try
        {
            JSONObject obj = new JSONObject(mockData);
            JSONObject userPosts = obj.getJSONObject(user.getUserName()).getJSONObject("posts");
            Iterator<?> keys = userPosts.keys();

            int count = 0;

            while(keys.hasNext() && count < n)
            {
                String key = (String)keys.next();
                JSONObject postObject = userPosts.getJSONObject(key);
                String timestamp = postObject.getString("timestamp");
                timestamp = Utils.smartTimestampFromUnixTime(Long.parseLong(timestamp));
                String content = postObject.getString("content");

                List<Comment> comments = new ArrayList<>();
                JSONArray commentArray = postObject.getJSONArray("comments");

                for(int i = 0; i < commentArray.length(); ++i)
                {
                    JSONObject commentObj = commentArray.getJSONObject(i);
                    Comment comment = new Comment(commentObj.getString("user"),
                            commentObj.getString("comment"));
                    comments.add(comment);
                }

                posts.add(new Post(user.getUserName(), timestamp, content, comments));
                count++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return posts;
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

    public static void updateProfile(Profile profile)
    /**
     * Encrypts new profile data and sends to database.
     *
     * @param profile Profile object with new profile data.
     */
    {

    }

    public static Profile getUserProfile(String userName)
    /**
     * Gets a user's profile.
     *
     * @param user User whose profile I'm getting.
     * @return Profile of designated user.
     */
    {
        Profile profile = null;

        try
        {
            JSONObject obj = new JSONObject(mockData);
            JSONObject profileObj = obj.getJSONObject(userName).getJSONObject("profile");

            String aboutMe = profileObj.getString("about_me");
            profile = new Profile(userName, aboutMe);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return profile;
    }

    public static void addCommentToPost(Post post, Comment comment)
    /**
     * Adds comment to existing post
     *
     * @param post Post I am adding comment to.
     * @param comment Comment that I'm adding.
     */
    {

    }
}