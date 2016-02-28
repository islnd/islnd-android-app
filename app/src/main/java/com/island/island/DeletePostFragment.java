package com.island.island;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.island.island.Database.IslandDB;

public class DeletePostFragment extends DialogFragment {
    private static final String TAG = DeletePostFragment.class.getSimpleName();

    public static final String USER_ID_BUNDLE_KEY = "USER_PARAM";
    public static final String POST_ID_BUNDLE_KEY = "POST_PARAM";

    private NoticeDeletePostListener mListener;

    public interface NoticeDeletePostListener {
        void onDeletePostDialogPositiveClick(DialogFragment dialogFragment);
    }

    public static DeletePostFragment buildWithArgs(int userId, String postId) {
        DeletePostFragment deletePostFragment = new DeletePostFragment();
        Bundle args = new Bundle();
        args.putInt(DeletePostFragment.USER_ID_BUNDLE_KEY, userId);
        args.putString(DeletePostFragment.POST_ID_BUNDLE_KEY, postId);
        deletePostFragment.setArguments(args);
        return deletePostFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setMessage(getString(R.string.delete_post_dialog))
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                        {
                            String postId = getArguments().getString(POST_ID_BUNDLE_KEY);
                            int postUserId = getArguments().getInt(USER_ID_BUNDLE_KEY);
                            mListener.onDeletePostDialogPositiveClick(this);

                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    IslandDB.deletePost(getActivity(), postUserId, postId);
                                    return null;
                                }
                            }.execute();
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(TAG, "attaching...");
        super.onAttach(activity);

        try {
            mListener = (NoticeDeletePostListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + "must implement " + NoticeDeletePostListener.class.getSimpleName());
        }
    }
}
