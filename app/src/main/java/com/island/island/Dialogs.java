package com.island.island;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.island.island.Activities.ShowQRActivity;
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
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                {
                    IslandDB.removeReader(removeFriend);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static void deletePostDialog(Context context, String postId)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.delete_post_dialog))
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                {
                    // TODO: Add remove post!
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

    public static void qrCodeActionDialog(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.qr_action_dialog)
                .setItems(R.array.qr_actions, (DialogInterface dialog, int which) ->
                {
                    switch (which) {
                        case 0: // Show QR
                            context.startActivity(new Intent(context, ShowQRActivity.class));
                            break;
                        case 1: // Get QR
                            break;
                    }
                })
                .show();
    }
}
