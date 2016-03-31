package io.islnd.android.islnd.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.messaging.message.MessagePublisher;

public class Dialogs {
    private static final String TAG = Dialogs.class.getSimpleName();

    public static void removeFriendDialog(Context context, int userId, String displayName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
        builder.setMessage("Remove " + displayName + " as a friend?")
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                {
                    MessagePublisher.removeFriend(context, userId);
                    DataUtils.markUserAsDeletedAndDeletePosts(context, userId);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
