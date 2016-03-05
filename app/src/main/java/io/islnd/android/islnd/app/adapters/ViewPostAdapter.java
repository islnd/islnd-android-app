package io.islnd.android.islnd.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.activities.ProfileActivity;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.database.ProfileDatabase;
import io.islnd.android.islnd.app.DeleteCommentDialog;
import io.islnd.android.islnd.app.models.CommentViewModel;
import io.islnd.android.islnd.app.models.PostKey;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.app.viewholders.CommentViewHolder;

public class ViewPostAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder>
{
    private static final String TAG = ViewPostAdapter.class.getSimpleName();

    private Context mContext = null;
    private final int mPostUserId;
    private final String mPostId;


    private static final int COMMENT = 1;

    public ViewPostAdapter(Context context, Cursor cursor, PostKey key)
    {
        super(context, cursor);
        mContext = context;
        mPostUserId = key.getUserId();
        mPostId = key.getPostId();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment, parent, false);
        return new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        final CommentViewModel comment = new CommentViewModel(
                cursor.getString(cursor.getColumnIndex(IslndContract.UserEntry.COLUMN_USERNAME)),
                cursor.getInt(cursor.getColumnIndex(IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID)),
                cursor.getString(cursor.getColumnIndex(IslndContract.CommentEntry.COLUMN_COMMENT_ID)),
                cursor.getString(cursor.getColumnIndex(IslndContract.CommentEntry.COLUMN_CONTENT)),
                cursor.getLong(cursor.getColumnIndex(IslndContract.CommentEntry.COLUMN_TIMESTAMP)));

        CommentViewHolder holder = (CommentViewHolder) viewHolder;
        final ImageView overflow = holder.overflow;

        holder.userName.setText(comment.getUsername());
        holder.comment.setText(comment.getComment());

        // Go to profile on picture click
        holder.profileImage.setOnClickListener(
                (View v) ->
                {
                    Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                    profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, comment.getUsername());
                    mContext.startActivity(profileIntent);
                });

        ProfileDatabase profileDatabase = ProfileDatabase.getInstance(mContext);
        Uri profileImageUri = Uri.parse(profileDatabase.getProfileImageUri(comment.getUsername()));
        ImageUtil.setCommentProfileImageSampled(mContext, holder.profileImage, profileImageUri);

        if (Util.isUser(mContext, comment.getUsername())) {
            holder.overflow.setVisibility(View.VISIBLE);

            holder.overflow.setOnClickListener(
                    (View v) ->
                    {
                        PopupMenu popup = new PopupMenu(mContext, overflow);
                        popup.getMenuInflater().inflate(R.menu.comment_menu, popup.getMenu());
                        popup.setOnMenuItemClickListener(
                                (MenuItem item) ->
                                {
                                    switch (item.getItemId()) {
                                        case R.id.delete_comment:
                                            DialogFragment deleteCommentFragment =
                                                    DeleteCommentDialog.buildWithArgs(
                                                            mPostUserId,
                                                            mPostId,
                                                            comment.getUserId(),
                                                            comment.getCommentId());
                                            deleteCommentFragment.show(
                                                    ((FragmentActivity) mContext).getSupportFragmentManager(),
                                                    mContext.getString(R.string.fragment_delete_comment));
                                    }

                                    return true;
                                });

                        popup.show();
                    });
        } else {
            holder.overflow.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return COMMENT;
    }
}
