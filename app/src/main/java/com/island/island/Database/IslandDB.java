package com.island.island.Database;

import android.util.JsonReader;

import com.island.island.Containers.Post;
import com.island.island.Containers.Profile;
import com.island.island.Containers.User;
import com.island.island.Utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public static String mockData = "{\n" +
            "  \"users\": [\n" +
            "    \"Bill Gates\", \"Steve Jobs\", \"Fred Flintstone\", \"John Smith\", \"Thom Yorke\"\n" +
            "  ],\n" +
            "  \"Bill Gates\": {\n" +
            "    \"posts\": {\n" +
            "      \"0\": {\n" +
            "        \"timestamp\": \"1453837198\",\n" +
            "        \"content\": \"I created Microsoft! I've got billions and dollars and I donate most of it to charity. Windows 10 is awesome. Vista was horrible.\",\n" +
            "        \"comments\": {}\n" +
            "      },\n" +
            "      \"1\": {\n" +
            "        \"timestamp\": \"1253837198\",\n" +
            "        \"content\": \"I made a bunch of money today!\",\n" +
            "        \"comments\": {}\n" +
            "      },\n" +
            "      \"2\": {\n" +
            "        \"timestamp\": \"1253937198\",\n" +
            "        \"content\": \"I made even more money today!\",\n" +
            "        \"comments\": {}\n" +
            "      }\n" +
            "    },\n" +
            "    \"profile\":\n" +
            "    {\n" +
            "      \"about_me\": \"Founder of Microsoft.\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"Steve Jobs\": {\n" +
            "    \"posts\": {\n" +
            "      \"0\": {\n" +
            "        \"timestamp\": \"1453837000\",\n" +
            "        \"content\": \"*** British Voice *** Steve Jobs was cutting edge. He changed computing as we know it. His innovations are more important that you.\",\n" +
            "        \"comments\": {}\n" +
            "      },\n" +
            "      \"1\": {\n" +
            "        \"timestamp\": \"1213857000\",\n" +
            "        \"content\": \"I'm innovative lol :)\",\n" +
            "        \"comments\": {}\n" +
            "      }\n" +
            "    },\n" +
            "    \"profile\":\n" +
            "    {\n" +
            "      \"about_me\": \"Founder of Apple.\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"Fred Flintstone\": {\n" +
            "    \"posts\": {\n" +
            "      \"0\": {\n" +
            "        \"timestamp\": \"1453830198\",\n" +
            "        \"content\": \"I am prehistoric. I know the guy that invented the wheel! My best friend is Barney. My pet dinosaur always tricks me.\",\n" +
            "        \"comments\": {}\n" +
            "      },\n" +
            "      \"1\": {\n" +
            "        \"timestamp\": \"1400830198\",\n" +
            "        \"content\": \"Watch my show tonight!\",\n" +
            "        \"comments\": {}\n" +
            "      }\n" +
            "    },\n" +
            "    \"profile\":\n" +
            "    {\n" +
            "      \"about_me\": \"Citizen of Bedrock.\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"John Smith\": {\n" +
            "    \"posts\": {\n" +
            "      \"0\": {\n" +
            "        \"timestamp\": \"1453807198\",\n" +
            "        \"content\": \"This post is boring just like my name :)\",\n" +
            "        \"comments\": {}\n" +
            "      }\n" +
            "    },\n" +
            "    \"profile\":\n" +
            "    {\n" +
            "      \"about_me\": \"I ain't nothin'.\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"Thom Yorke\": {\n" +
            "    \"posts\": {\n" +
            "      \"0\": {\n" +
            "        \"timestamp\": \"1450837198\",\n" +
            "        \"content\": \"I am radiohead lol. I have a high voice but it's cool because I'm nasty with a synth. Get at me Damon Albarn. Gorillaz suk lol ayyyyyyeeeee. ISLAND NEEDS TO SUPPORT EMOJIS AYYYYEEEEe.\",\n" +
            "        \"comments\": {}\n" +
            "      }\n" +
            "    },\n" +
            "    \"profile\":\n" +
            "    {\n" +
            "      \"about_me\": \"I'm in that one band.\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public static List<User> getUsers()
    /**
     * Gets list of users that have allowed me to read their content.
     *
     * @return The list of users.
     */
    {
        List<User> userList = new ArrayList<>();

        try
        {
            JSONObject obj = new JSONObject(mockData);
            JSONArray users = obj.getJSONArray("users");

            for(int i = 0; i < users.length(); ++i)
            {
                User newUser = new User(users.getString(i), "", "");
                userList.add(newUser);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userList;
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
            JSONObject userPosts = obj.getJSONObject(user.getUsername()).getJSONObject("posts");
            Iterator<?> keys = userPosts.keys();

            while(keys.hasNext())
            {
                String key = (String)keys.next();
                JSONObject postObject = userPosts.getJSONObject(key);
                String timestamp = postObject.getString("timestamp");
                timestamp = Utils.smartTimestampFromUnixTime(Long.parseLong(timestamp));
                String content = postObject.getString("content");

                posts.add(new Post(user.getUsername(), timestamp, content, ""));
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
            JSONObject userPosts = obj.getJSONObject(user.getUsername()).getJSONObject("posts");
            Iterator<?> keys = userPosts.keys();

            int count = 0;

            while(keys.hasNext() && count < n)
            {
                String key = (String)keys.next();
                JSONObject postObject = userPosts.getJSONObject(key);
                String timestamp = postObject.getString("timestamp");
                timestamp = Utils.smartTimestampFromUnixTime(Long.parseLong(timestamp));
                String content = postObject.getString("content");

                posts.add(new Post(user.getUsername(), timestamp, content, ""));
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
            profile = new Profile(userName, "", "", aboutMe);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return profile;
    }
}