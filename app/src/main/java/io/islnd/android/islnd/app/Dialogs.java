package io.islnd.android.islnd.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import io.islnd.android.islnd.app.database.IslndDb;

public class Dialogs
{
    private static final String TAG = Dialogs.class.getSimpleName();

    public static void removeFriendDialog(Context context, int userId, String displayName)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Remove " + displayName + " as a friend?")
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                {
                    IslndDb.removeReader(userId);
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
