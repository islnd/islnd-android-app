package io.islnd.android.islnd.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.List;

import io.islnd.android.islnd.app.database.IslndDb;
import io.islnd.android.islnd.messaging.event.Event;
import io.islnd.android.islnd.messaging.event.EventListBuilder;
import io.islnd.android.islnd.messaging.event.EventProcessor;

public class DeletePostDialog extends DialogFragment {
    private static final String TAG = DeletePostDialog.class.getSimpleName();

    public static final String USER_ID_BUNDLE_KEY = "USER_PARAM";
    public static final String POST_ID_BUNDLE_KEY = "POST_PARAM";

    private boolean mFinishActivity;

    public static DeletePostDialog buildWithArgs(int userId, String postId) {
        DeletePostDialog deletePostDialog = new DeletePostDialog();
        Bundle args = new Bundle();
        args.putInt(DeletePostDialog.USER_ID_BUNDLE_KEY, userId);
        args.putString(DeletePostDialog.POST_ID_BUNDLE_KEY, postId);
        deletePostDialog.setArguments(args);
        return deletePostDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        return builder.setMessage(getString(R.string.delete_post_dialog))
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                        {
                            String postId = getArguments().getString(POST_ID_BUNDLE_KEY);
                            final Context context = getContext();
                            List<Event> deletePostEvents = new EventListBuilder(context)
                                    .deletePost(postId)
                                    .build();

                            for (Event event : deletePostEvents) {
                                EventProcessor.process(context, event);

                                Intent pushEventService = new Intent(context, EventPushService.class);
                                pushEventService.putExtra(EventPushService.EVENT_EXTRA, event);
                                context.startService(pushEventService);
                            }

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
