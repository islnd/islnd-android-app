package com.island.island.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.island.island.Activities.FeedActivity;
import com.island.island.Activities.ProfileActivity;
import com.island.island.Activities.ViewPostActivity;
import com.island.island.Database.IslndContract;
import com.island.island.Database.ProfileDatabase;
import com.island.island.DeletePostFragment;
import com.island.island.Models.Post;
import com.island.island.R;
import com.island.island.Utils.ImageUtils;
import com.island.island.Utils.Utils;
import com.island.island.ViewHolders.FriendGlanceViewHolder;
import com.island.island.ViewHolders.GlancePostViewHolder;

import java.util.ArrayList;
import java.util.List;

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
                "Fake Name",
                cursor.getInt(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_USER_ID)),
                cursor.getString(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_POST_ID)),
                cursor.getLong(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_TIMESTAMP)),
                cursor.getString(cursor.getColumnIndex(IslndContract.PostEntry.COLUMN_CONTENT)),
                new ArrayList<>()
        );

        holder.postUserName.setText(post.getUserName());
        holder.postTimestamp.setText(Utils.smartTimestampFromUnixTime(post.getTimestamp()));
        holder.postContent.setText(post.getContent());
        holder.postCommentCount.setText(Utils.numberOfCommentsString(post.getComments().size()));

        // Go to profile on picture click
        holder.postProfileImage.setOnClickListener((View v) -> {
            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
            profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, post.getUserName());
            mContext.startActivity(profileIntent);
        });

        ProfileDatabase profileDatabase = ProfileDatabase.getInstance(mContext);
        Uri profileImageUri = Uri.parse(profileDatabase.getProfileImageUri(post.getUserName()));
        ImageUtils.setPostProfileImageSampled(mContext, holder.postProfileImage, profileImageUri);

        // View post on post click
        holder.itemView.setOnClickListener((View v) -> {
            Intent viewPostIntent = new Intent(mContext, ViewPostActivity.class);
            viewPostIntent.putExtra(Post.POST_EXTRA, post);
            ((Activity)mContext).startActivityForResult(viewPostIntent, FeedActivity.DELETE_POST_RESULT);
        });

        if(Utils.isUser(mContext, post.getUserName())) {
            holder.postOverflow.setVisibility(View.VISIBLE);

            holder.postOverflow.setOnClickListener((View v) -> {
                PopupMenu popup = new PopupMenu(mContext, holder.postOverflow);
                popup.getMenuInflater().inflate(R.menu.post_menu, popup.getMenu());
                popup.setOnMenuItemClickListener((MenuItem item) -> {
                    switch (item.getItemId()) {
                        case R.id.delete_post:
                            DialogFragment deletePostFragment =
                                    DeletePostFragment.buildWithArgs(post.getUserId(), post.getPostId());
                            deletePostFragment.show(
                                    ((FragmentActivity) mContext).getSupportFragmentManager(),
                                    mContext.getString(R.string.fragment_delete_post));
                    }

                    return true;
                });

                popup.show();
            });
        } else {
            holder.postOverflow.setVisibility(View.GONE);
        }
    }
}
