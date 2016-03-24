package io.islnd.android.islnd.app.adapters;

import android.app.Activity;
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

import io.islnd.android.islnd.app.DeletePostDialog;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.activities.ProfileActivity;
import io.islnd.android.islnd.app.activities.ViewPostActivity;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.fragments.FeedFragment;
import io.islnd.android.islnd.app.models.Post;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.app.viewholders.GlancePostViewHolder;

public class PostAdapter extends CursorRecyclerViewAdapter<GlancePostViewHolder> {

    public PostAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public GlancePostViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.glance_post, parent, false);

        GlancePostViewHolder viewHolder = new GlancePostViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(GlancePostViewHolder holder, Cursor cursor) {
        Post post = new Post(
                cursor.getString(cursor.getColumnIndex(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME)),
                cursor.getInt(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_USER_ID)),
                cursor.getString(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_ALIAS)),
                cursor.getString(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_POST_ID)),
                cursor.getLong(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_TIMESTAMP)),
                cursor.getString(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_CONTENT)),
                cursor.getInt(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_COMMENT_COUNT)));

        holder.postUserName.setText(post.getDisplayName());
        holder.postTimestamp.setText(Util.smartTimestampFromUnixTime(mContext, post.getTimestamp()));
        holder.postContent.setText(post.getContent());
        holder.postCommentCount.setText(Util.numberOfCommentsString(post.getCommentCount()));
        
        holder.postProfileImage.setOnClickListener((View v) -> {
            if (mContext instanceof ProfileActivity) {
                return;
            }
            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
            profileIntent.putExtra(ProfileActivity.USER_ID_EXTRA, post.getUserId());
            mContext.startActivity(profileIntent);
        });

        ImageUtil.setPostProfileImageSampled(
                mContext,
                holder.postProfileImage,
                Uri.parse(cursor.getString(cursor.getColumnIndex(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI))));

        // View post on post click
        holder.itemView.setOnClickListener((View v) -> {
            Intent viewPostIntent = new Intent(mContext, ViewPostActivity.class);
            viewPostIntent.putExtra(Post.POST_EXTRA, post);
            ((Activity)mContext).startActivityForResult(viewPostIntent, FeedFragment.DELETE_POST_RESULT);
        });

        if(Util.isUser(mContext, post.getUserId())) {
            holder.view.setOnLongClickListener((View v) -> {
                final String DELETE_POST = mContext.getString(R.string.delete_post);
                final String[] items = {DELETE_POST};

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppTheme_Dialog);
                builder.setItems(items, (DialogInterface dialog, int item) -> {
                    String itemStr = items[item];
                    if (itemStr.equals(DELETE_POST)) {
                        DialogFragment deletePostFragment =
                                DeletePostDialog.buildWithArgs(post.getPostId());
                        deletePostFragment.show(
                                ((FragmentActivity) mContext).getSupportFragmentManager(),
                                mContext.getString(R.string.fragment_delete_post));
                    }
                }).show();

                return true;
            });
        }
    }
}
