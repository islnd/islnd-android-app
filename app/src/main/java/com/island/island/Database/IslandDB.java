package com.island.island.Database;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.island.island.Models.Comment;
import com.island.island.Models.Post;
import com.island.island.Models.Profile;
import com.island.island.Models.User;
import com.island.island.Utils.Utils;

import org.island.messaging.Crypto;
import org.island.messaging.MessageLayer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by David Thompson on 12/20/2015.
 *
 * This class holds the functions to talk to the database.
 */
public class IslandDB
{
    // Used json minifier online
    public static String mockData = "{\"users\":[\"Bill Gates\",\"Steve Jobs\",\"Fred Flintstone\",\"John Smith\",\"Thom Yorke\"],\"Bill Gates\":{\"posts\":{\"0\":{\"timestamp\":\"1453837198\",\"content\":\"I created Microsoft! I've got billions and dollars and I donate most of it to charity. Windows 10 is awesome. Vista was horrible.\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"},{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]},\"1\":{\"timestamp\":\"1253837198\",\"content\":\"I made a bunch of money today!\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"},{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]},\"2\":{\"timestamp\":\"1253937198\",\"content\":\"I made even more money today!\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"},{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]}},\"profile\":{\"about_me\":\"Founder of Microsoft.\"}},\"Steve Jobs\":{\"posts\":{\"0\":{\"timestamp\":\"1453837000\",\"content\":\"*** British Voice *** Steve Jobs was cutting edge. He changed computing as we know it. His innovations are more important that you.\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]},\"1\":{\"timestamp\":\"1213857000\",\"content\":\"I'm innovative lol :)\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]}},\"profile\":{\"about_me\":\"Founder of Apple.\"}},\"Fred Flintstone\":{\"posts\":{\"0\":{\"timestamp\":\"1453830198\",\"content\":\"I am prehistoric. I know the guy that invented the wheel! My best friend is Barney. My pet dinosaur always tricks me.\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"},{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]},\"1\":{\"timestamp\":\"1400830198\",\"content\":\"Watch my show tonight!\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]}},\"profile\":{\"about_me\":\"Citizen of Bedrock.\"}},\"John Smith\":{\"posts\":{\"0\":{\"timestamp\":\"1453807198\",\"content\":\"This post is boring just like my name :)\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]}},\"profile\":{\"about_me\":\"I ain't nothin'.\"}},\"Thom Yorke\":{\"posts\":{\"0\":{\"timestamp\":\"1450837198\",\"content\":\"I am radiohead lol. I have a high voice but it's cool because I'm nasty with a synth. Get at me Damon Albarn. Gorillaz suk lol ayyyyyyeeeee. ISLAND NEEDS TO SUPPORT EMOJIS AYYYYEEEEe.\",\"comments\":[{\"user\":\"Steve Jobs\",\"comment\":\"Microsoft suks!!!\"},{\"user\":\"Thom Yorke\",\"comment\":\"You inspired Okay Computer\"},{\"user\":\"Fred Flintstone\",\"comment\":\"YOU ARE A MAGICIAN!!!!!!!!!!!!!!\"},{\"user\":\"John Smith\",\"comment\":\"Nice weather we're having.\"}]}},\"profile\":{\"about_me\":\"I'm in that one band.\"}}}";
    private static IdentityDatabase IDENTITY_DB;

    public static List<User> getUsers(Context context)
    {
//        AsyncTask<Void, Void, List<User>> task = new AsyncTask<Void, Void, List<User>>() {
//            @Override
//            protected List<User> doInBackground(Void... params) {
//                return MessageLayer.getReaders(getIdentityDatabase(context).getUsername());
//            }
//        };

//        try {
//            return task.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        return null;
    }

    public static void postPublicKey(Context context)
    {
        IdentityDatabase identityDatabase = getIdentityDatabase(context);
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                MessageLayer.postPublicKey(identityDatabase.getUsername(), identityDatabase.getPublicKey());
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
        if(getIdentityDatabase(context).getPublicKey() != null) {
            return;
        }
        KeyPair keyPair = Crypto.getKeyPair();
        IdentityDatabase identityDatabase = getIdentityDatabase(context);
        identityDatabase.setIdentity(keyPair.getPublic(), keyPair.getPrivate(), username);
    }

    public static void post(String content)
    /**
     * Encrypts content and posts to database.
     *
     * @param content Plaintext content to be posted.
     */
    {

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