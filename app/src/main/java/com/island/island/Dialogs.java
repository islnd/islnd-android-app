package com.island.island;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;
import com.island.island.Activities.FeedActivity;
import com.island.island.Activities.ShowQRActivity;
import com.island.island.Database.IslandDB;
import com.island.island.Models.Post;
import com.island.island.Models.PostKey;
import com.island.island.Utils.Utils;

import java.util.ArrayList;

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

    public static void deleteCommentDialog(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.delete_comment_dialog))
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                {
                        // TODO: Add remove comment!
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
