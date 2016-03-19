package io.islnd.android.islnd.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.List;

import io.islnd.android.islnd.messaging.event.Event;
import io.islnd.android.islnd.messaging.event.EventListBuilder;
import io.islnd.android.islnd.messaging.event.EventProcessor;

public class DeleteCommentDialog extends DialogFragment {
    private static final String POST_USER_ID_BUNDLE_KEY = "POST_USER_ID_PARAM";
    private static final String POST_ID_BUNDLE_KEY = "POST_ID_PARAM";
    public static final String COMMENT_USER_ID_BUNDLE_KEY = "COMMENT_USER_ID_PARAM";
    public static final String COMMENT_ID_BUNDLE_KEY = "COMMENT_ID_PARAM";

    public static DialogFragment buildWithArgs(
            int postAuthorUserId,
            String postId,
            int commentAuthorUserId,
            String commentId) {
        DeleteCommentDialog deleteCommentDialog = new DeleteCommentDialog();
        Bundle args = new Bundle();
        args.putInt(DeleteCommentDialog.POST_USER_ID_BUNDLE_KEY, postAuthorUserId);
        args.putString(DeleteCommentDialog.POST_ID_BUNDLE_KEY, postId);
        args.putInt(DeleteCommentDialog.COMMENT_USER_ID_BUNDLE_KEY, commentAuthorUserId);
        args.putString(DeleteCommentDialog.COMMENT_ID_BUNDLE_KEY, commentId);
        deleteCommentDialog.setArguments(args);
        return deleteCommentDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        return builder.setMessage(getString(R.string.delete_comment_dialog))
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int id) ->
                        {
                            String commentId = getArguments().getString(COMMENT_ID_BUNDLE_KEY);
                            final Context context = getContext();
                            List<Event> deleteCommentEvents = new EventListBuilder(context)
                                    .deleteComment(commentId)
                                    .build();

                            for (Event event : deleteCommentEvents) {
                                EventProcessor.process(context, event);
                                Intent pushEventService = new Intent(context, EventPushService.class);
                                pushEventService.putExtra(EventPushService.EVENT_EXTRA, event);
                                context.startService(pushEventService);
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
