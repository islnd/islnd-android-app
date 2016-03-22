package io.islnd.android.islnd.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import io.islnd.android.islnd.app.activities.ProfileActivity;
import io.islnd.android.islnd.app.Dialogs;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.models.User;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.viewholders.FriendGlanceViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ViewFriendsAdapter extends CursorRecyclerViewAdapter<FriendGlanceViewHolder>
{
    private Context mContext = null;

    public ViewFriendsAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.mContext = context;
    }

    @Override
    public FriendGlanceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_glance, parent, false);

        FriendGlanceViewHolder viewHolder = new FriendGlanceViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FriendGlanceViewHolder holder, Cursor cursor) {
        final User user = new User(
                cursor.getInt(cursor.getColumnIndex(IslndContract.UserEntry._ID)),
                cursor.getString(cursor.getColumnIndex(IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME))
        );

        holder.userName.setText(user.getDisplayName());

        // Go to profile on view click
        holder.itemView.setOnClickListener((View v) ->
                {
                    Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                    profileIntent.putExtra(ProfileActivity.USER_ID_EXTRA, user.getUserId());
                    mContext.startActivity(profileIntent);
                });

        ImageUtil.setViewFriendImageSampled(
                mContext,
                holder.profileImage,
                Uri.parse(cursor.getString(cursor.getColumnIndex(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI))));

        holder.overflow.setOnClickListener((View v) ->
        {
            final int REMOVE_FRIEND = 0;

            PopupMenu popup = new PopupMenu(mContext, holder.overflow);

            popup.getMenu().add(0, REMOVE_FRIEND, 0, mContext.getString(R.string.remove_friend));

            popup.setOnMenuItemClickListener((MenuItem item) ->
            {
                switch (item.getItemId())
                {
                    case REMOVE_FRIEND:
                        Dialogs.removeFriendDialog(mContext, user.getUserId(), user.getDisplayName());
                }

                return true;
            });

            popup.show();
        });
    }
}
