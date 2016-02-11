package com.island.island.Models;

import java.io.Serializable;

/**
 * Created by David Thompson on 12/20/2015.
 *
 * This class represents a user.
 */
public class User implements Serializable, Comparable<User>
{
    private String userName = "";
    private String pseudonym = "";
    private String groupKey = "";

    public void User(){}

    public User(String userName, String pseudonym, String groupKey)
    {
        this.userName = userName;
        this.pseudonym = pseudonym;
        this.groupKey = groupKey;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getPseudonym()
    {
        return pseudonym;
    }

    public String getGroupKey()
    {
        return groupKey;
    }

    public void setUserName(String username)
    {
        this.userName = username;
    }

    public void setPseudonym(String pseudonym)
    {
        this.pseudonym = pseudonym;
    }

    public void setGroupKey(String groupKey)
    {
        this.groupKey = groupKey;
    }

    @Override
    public int compareTo(User another)
    {
        return userName.compareTo(another.getUserName());
    }
}
