package com.island.island.Containers;

/**
 * Created by poo on 1/29/2016.
 */
public class Comment
{
    private String userName = "";
    private String comment = "";

    public Comment(String userName, String comment)
    {
        this.userName = userName;
        this.comment = comment;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }
}
