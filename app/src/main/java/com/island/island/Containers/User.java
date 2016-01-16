package com.island.island.Containers;

/**
 * Created by David Thompson on 12/20/2015.
 *
 * This class represents a user.
 */
public class User
{
    private String username = "";
    private String pseudonym = "";
    private String groupKey = "";

    public void User(){}

    public void User(String username, String pseudonym, String groupKey)
    {
        this.username = username;
        this.pseudonym = pseudonym;
        this.groupKey = groupKey;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPseudonym()
    {
        return pseudonym;
    }

    public String getGroupKey()
    {
        return groupKey;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPseudonym(String pseudonym)
    {
        this.pseudonym = pseudonym;
    }

    public void setGroupKey(String groupKey)
    {
        this.groupKey = groupKey;
    }
}
