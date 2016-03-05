package com.island.island;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.island.island.Database.IslandDB;

public class DeleteCommentFragment extends DialogFragment {
    private static final String POST_USER_ID_BUNDLE_KEY = "POST_USER_ID_PARAM";
    private static final String POST_ID_BUNDLE_KEY = "POST_ID_PARAM";
    public static final String COMMENT_USER_ID_BUNDLE_KEY = "COMMENT_USER_ID_PARAM";
    public static final String COMMENT_ID_BUNDLE_KEY = "COMMENT_ID_PARAM";

    public static DialogFragment buildWithArgs(
            int postAuthorUserId,
            String postId,
            int commentAuthorUserId,
            String commentId) {
        DeleteCommentFragment deleteCommentFragment = new DeleteCommentFragment();
        Bundle args = new Bundle();
        args.putInt(DeleteCommentFragment.POST_USER_ID_BUNDLE_KEY, postAuthorUserId);
        args.putString(DeleteCommentFragment.POST_ID_BUNDLE_KEY, postId);
        args.putInt(DeleteCommentFragment.COMMENT_USER_ID_BUNDLE_KEY, commentAuthorUserId);
        args.putString(DeleteCommentFragment.COMMENT_ID_BUNDLE_KEY, commentId);
        deleteCommentFragment.setArguments(args);
        return deleteCommentFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setMessage(getString(R.string.delete_comment_dialog))
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                        {
                            String postId = getArguments().getString(POST_ID_BUNDLE_KEY);
                            int postUserId = getArguments().getInt(POST_USER_ID_BUNDLE_KEY);
                            String commentId = getArguments().getString(COMMENT_ID_BUNDLE_KEY);
                            int commentUserId = getArguments().getInt(COMMENT_USER_ID_BUNDLE_KEY);

                            //--Send delete to server
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    IslandDB.deleteComment(
                                            getActivity(),
                                            postUserId,
                                            postId,
                                            commentUserId,
                                            commentId);
                                    return null;
                                }
                            }.execute();
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
