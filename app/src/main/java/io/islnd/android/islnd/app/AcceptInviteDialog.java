package io.islnd.android.islnd.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.messaging.MessageLayer;

public class AcceptInviteDialog extends DialogFragment {
    private static final String TAG = AcceptInviteDialog.class.getSimpleName();

    public static final String INVITE_ID_BUNDLE_KEY = "INVITE_PARAM";

    public static AcceptInviteDialog buildWithArgs(long inviteId) {
        AcceptInviteDialog acceptInviteDialog = new AcceptInviteDialog();
        Bundle args = new Bundle();
        args.putLong(AcceptInviteDialog.INVITE_ID_BUNDLE_KEY, inviteId);
        acceptInviteDialog.setArguments(args);
        return acceptInviteDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        return builder.setMessage(getString(R.string.accept_invite_dialog))
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                        {
                            long inviteId = getArguments().getLong(INVITE_ID_BUNDLE_KEY);
                            acceptAndDeleteInvite(inviteId);
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private void acceptAndDeleteInvite(long inviteId) {
        final Context context = getContext();
        String inviteContents = DataUtils.getInvite(context, inviteId);
        DataUtils.deleteInvite(context, inviteId);
        Log.v(TAG, "accepting invite " + inviteContents);
        MessageLayer.addPublicIdentityFromSms(context, inviteContents);
    }
}
