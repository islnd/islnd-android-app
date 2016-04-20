package io.islnd.android.islnd.app.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.islnd.android.islnd.app.AcceptInviteDialog;
import io.islnd.android.islnd.app.DeletePostDialog;
import io.islnd.android.islnd.app.NotificationHelper;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.activities.ProfileActivity;
import io.islnd.android.islnd.app.activities.ViewPostActivity;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.database.NotificationType;
import io.islnd.android.islnd.app.models.Post;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.app.viewholders.InviteViewHolder;
import io.islnd.android.islnd.app.viewholders.NotificationViewHolder;

public class InviteAdapter extends CursorRecyclerViewAdapter<InviteViewHolder> {

    private static final String TAG = InviteAdapter.class.getSimpleName();

    public InviteAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(InviteViewHolder holder, Cursor cursor) {
        String displayName = cursor.getString(cursor.getColumnIndex(IslndContract.InviteEntry.COLUMN_DISPLAY_NAME));
        String phoneNumber = cursor.getString(cursor.getColumnIndex(IslndContract.InviteEntry.COLUMN_PHONE_NUMBER));
        long inviteId = cursor.getLong(cursor.getColumnIndex(IslndContract.InviteEntry._ID));
        holder.displayName.setText(displayName);
        holder.phoneNumber.setText(phoneNumber);

        holder.view.setOnClickListener((View v) -> {
            DialogFragment acceptInviteDialog =
                    AcceptInviteDialog.buildWithArgs(inviteId);
            acceptInviteDialog.show(
                    ((FragmentActivity) mContext).getSupportFragmentManager(),
                    mContext.getString(R.string.fragment_accept_invite));
        });
    }

    @Override
    public InviteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.invite, parent, false);

        InviteViewHolder viewHolder = new InviteViewHolder(v);

        return viewHolder;
    }
}
