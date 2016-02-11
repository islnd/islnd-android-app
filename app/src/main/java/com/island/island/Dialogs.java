package com.island.island;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.island.island.Database.IslandDB;

/**
 * Created by poo on 2/10/2016.
 */
public class Dialogs
{
    public static void removeFriendDialog(Context context, String userName)
    {
        final String removeFriend = userName;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Remove " + userName + " as a friend?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        IslandDB.removeReader(removeFriend);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // User cancelled the dialog
                    }
                })
                .show();
    }
}
