package io.islnd.android.islnd.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class DeletePostDialog extends DialogFragment {
    private static final String TAG = DeletePostDialog.class.getSimpleName();

    public static final String POST_ID_BUNDLE_KEY = "POST_PARAM";

    private boolean mFinishActivity;

    public static DeletePostDialog buildWithArgs(String postId) {
        DeletePostDialog deletePostDialog = new DeletePostDialog();
        Bundle args = new Bundle();
        args.putString(DeletePostDialog.POST_ID_BUNDLE_KEY, postId);
        deletePostDialog.setArguments(args);
        return deletePostDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        return builder.setTitle(getString(R.string.dialog_title_delete_post))
                .setMessage(getString(R.string.dialog_delete_post))
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                        {
                            String postId = getArguments().getString(POST_ID_BUNDLE_KEY);
                            final Context context = getContext();
                            new EventPublisher(context)
                                    .deletePost(postId)
                                    .publish();

                            if (mFinishActivity) {
                                getActivity().finish();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    public void setFinishActivityIfSuccess(boolean finishActivity) {
        mFinishActivity = finishActivity;
    }
}
