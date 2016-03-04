package com.island.island;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.island.island.Database.IslandDB;

public class Dialogs
{
    private static final String TAG = Dialogs.class.getSimpleName();

    public static void removeFriendDialog(Context context, String userName)
    {
        final String removeFriend = userName;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Remove " + userName + " as a friend?")
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                {
                    IslandDB.removeReader(removeFriend);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static void allowUserDialog(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.allow_user_dialog))
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                {
                    // TODO: Allow user!
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
