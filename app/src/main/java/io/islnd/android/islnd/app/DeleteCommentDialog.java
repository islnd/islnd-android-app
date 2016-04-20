package io.islnd.android.islnd.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class DeleteCommentDialog extends DialogFragment {
    public static final String COMMENT_ID_BUNDLE_KEY = "COMMENT_ID_PARAM";

    public static DialogFragment buildWithArgs(String commentId) {
        DeleteCommentDialog deleteCommentDialog = new DeleteCommentDialog();
        Bundle args = new Bundle();
        args.putString(DeleteCommentDialog.COMMENT_ID_BUNDLE_KEY, commentId);
        deleteCommentDialog.setArguments(args);
        return deleteCommentDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        return builder.setTitle(getString(R.string.dialog_title_delete_comment))
                .setMessage(getString(R.string.dialog_delete_comment))
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                        {
                            String commentId = getArguments().getString(COMMENT_ID_BUNDLE_KEY);
                            final Context context = getContext();
                            new EventPublisher(context)
                                    .deleteComment(commentId)
                                    .publish();
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
