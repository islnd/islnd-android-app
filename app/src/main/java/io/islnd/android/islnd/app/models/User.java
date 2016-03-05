package io.islnd.android.islnd.app.models;

import java.io.Serializable;

/**
 * Created by David Thompson on 12/20/2015.
 *
 * This class represents a user.
 */
public class User implements Serializable, Comparable<User>
{
    private String userName = "";

    public void User(){}

    public User(String userName)
    {
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String username)
    {
        this.userName = username;
    }

    @Override
    public int compareTo(User another)
    {
        return userName.compareTo(another.getUserName());
    }
}
