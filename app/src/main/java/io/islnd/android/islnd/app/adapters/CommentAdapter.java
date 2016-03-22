package io.islnd.android.islnd.app.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.activities.ProfileActivity;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.DeleteCommentDialog;
import io.islnd.android.islnd.app.models.CommentViewModel;
import io.islnd.android.islnd.app.models.PostKey;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.app.viewholders.CommentViewHolder;

public class CommentAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = CommentAdapter.class.getSimpleName();

    private Context mContext = null;
    private final int mPostUserId;
    private final String mPostId;

    public CommentAdapter(Context context, Cursor cursor, PostKey key) {
        super(context, cursor);
        mContext = context;
        mPostUserId = key.getUserId();
        mPostId = key.getPostId();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment, parent, false);
        return new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        final CommentViewModel comment = new CommentViewModel(
                cursor.getString(cursor.getColumnIndex(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME)),
                cursor.getInt(cursor.getColumnIndex(IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID)),
                cursor.getString(cursor.getColumnIndex(IslndContract.CommentEntry.COLUMN_COMMENT_ID)),
                cursor.getString(cursor.getColumnIndex(IslndContract.CommentEntry.COLUMN_CONTENT)),
                cursor.getLong(cursor.getColumnIndex(IslndContract.CommentEntry.COLUMN_TIMESTAMP)));

        CommentViewHolder holder = (CommentViewHolder) viewHolder;

        holder.userName.setText(comment.getUsername());
        holder.comment.setText(comment.getComment());
        holder.timestamp.setText(Util.smartTimestampFromUnixTime(mContext, comment.getTimestamp()));

        // Go to profile on picture click
        holder.profileImage.setOnClickListener((View v) -> {
            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
            profileIntent.putExtra(ProfileActivity.USER_ID_EXTRA, comment.getUserId());
            mContext.startActivity(profileIntent);
        });

        ImageUtil.setCommentProfileImageSampled(
                mContext,
                holder.profileImage,
                Uri.parse(cursor.getString(cursor.getColumnIndex(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI))));

        if (Util.isUser(mContext, comment.getUserId())) {
            holder.view.setOnLongClickListener((View v) -> {
                final String DELETE_COMMENT = mContext.getString(R.string.delete_comment);
                final String[] items = {DELETE_COMMENT};

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppTheme_Dialog);
                builder.setItems(items, (DialogInterface dialog, int item) -> {
                    String itemStr = items[item];
                    if (itemStr.equals(DELETE_COMMENT)) {
                        DialogFragment deleteCommentFragment =
                                DeleteCommentDialog.buildWithArgs(comment.getCommentId());
                        deleteCommentFragment.show(
                                ((FragmentActivity) mContext).getSupportFragmentManager(),
                                mContext.getString(R.string.fragment_delete_comment));
                    }
                }).show();

                return true;
            });
        }
    }
}
