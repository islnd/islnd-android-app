package com.island.island;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Thompson on 12/20/2015.
 *
 * This class holds the functions to talk to the database.
 */
public class IslandDB
{
    public static List<User> getUsers()
    /**
     * Gets list of users that have allowed me to read their content.
     *
     * @return The list of users.
     */
    {
        // Return empty list for now
        List<User> emptyList = new ArrayList<>();

        return emptyList;
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
        List<Post> emptyList = new ArrayList<>();

        return emptyList;
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
        List<Post> emptyList = new ArrayList<>();

        return emptyList;
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

    public static Profile getUserProfile(User user)
    /**
     * Gets a user's profile.
     *
     * @param user User whose profile I'm getting.
     * @return Profile of designated user.
     */
    {
        // Return empty profile for now.
        Profile profile = new Profile();

        return profile;
    }
}