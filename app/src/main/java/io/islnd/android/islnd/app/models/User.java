package io.islnd.android.islnd.app.models;

import java.io.Serializable;

/**
 * Created by David Thompson on 12/20/2015.
 *
 * This class represents a user.
 */
public class User implements Serializable, Comparable<User>
{
    private final int userId;
    private final String displayName;

    public User(int userId, String displayName)
    {
        this.userId = userId;
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public int compareTo(User another)
    {
        return displayName.compareTo(another.getDisplayName());
    }

    public int getUserId() {
        return userId;
    }
}
