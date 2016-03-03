package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.island.island.Activities.ProfileActivity;
import com.island.island.Database.ProfileDatabase;
import com.island.island.Dialogs;
import com.island.island.Models.User;
import com.island.island.R;
import com.island.island.Utils.ImageUtils;
import com.island.island.ViewHolders.FriendGlanceViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ViewFriendsAdapter extends RecyclerView.Adapter<FriendGlanceViewHolder>
{
    private List<User> mList = new ArrayList<>();
    private Context mContext = null;

    public ViewFriendsAdapter(List<User> list, Context context)
    {
        mList = list;
        mContext = context;
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
    public void onBindViewHolder(FriendGlanceViewHolder holder, final int position)
    {
        User user = mList.get(position);

        holder.userName.setText(user.getUserName());

        // Go to profile on view click
        holder.itemView.setOnClickListener((View v) ->
        {
            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
            profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, user.getUserName());
            mContext.startActivity(profileIntent);
        });

        ProfileDatabase profileDatabase = ProfileDatabase.getInstance(mContext);
        Uri profileImageUri = Uri.parse(profileDatabase.getProfileImageUri(user.getUserName()));
        ImageUtils.setViewFriendImageSampled(mContext, holder.profileImage, profileImageUri);

        holder.overflow.setOnClickListener((View v) ->
        {
            final int REMOVE_FRIEND = 0;
            final int ALLOW_USER = 1;

            PopupMenu popup = new PopupMenu(mContext, holder.overflow);

            // TODO: if(user is allowed)
            popup.getMenu().add(0, REMOVE_FRIEND, 0, mContext.getString(R.string.remove_friend));

            // TODO: if(user is not allowed)
            popup.getMenu().add(0, ALLOW_USER, 1, mContext.getString(R.string.allow_user));

            popup.setOnMenuItemClickListener((MenuItem item) ->
            {
                switch (item.getItemId())
                {
                    case REMOVE_FRIEND:
                        Dialogs.removeFriendDialog(mContext, user.getUserName());
                        // TODO: Update UI to show user was removed
                    case ALLOW_USER:
                        Dialogs.allowUserDialog(mContext);
                        // TODO: Update UI to show that user was allowed
                }

                return true;
            });

            popup.show();
        });
    }

    @Override
    public int getItemCount()
    {
        return mList.size();
    }

    public User removeItem(int position)
    {
        final User user = mList.remove(position);
        notifyItemRemoved(position);
        return user;
    }

    public void addItem(int position, User user)
    {
        mList.add(position, user);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition)
    {
        final User user = mList.remove(fromPosition);
        mList.add(toPosition, user);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<User> users)
    {
        applyAndAnimateRemovals(users);
        applyAndAnimateAdditions(users);
        applyAndAnimateMovedItems(users);
    }

    private void applyAndAnimateRemovals(List<User> users)
    {
        for (int i = mList.size() - 1; i >= 0; i--)
        {
            final User user = mList.get(i);
            if (!users.contains(user))
            {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<User> users)
    {
        for (int i = 0, count = users.size(); i < count; i++)
        {
            final User user = users.get(i);
            if (!mList.contains(user))
            {
                addItem(i, user);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<User> users)
    {
        for (int toPosition = users.size() - 1; toPosition >= 0; toPosition--)
        {
            final User user = users.get(toPosition);
            final int fromPosition = users.indexOf(user);
            if (fromPosition >= 0 && fromPosition != toPosition)
            {
                moveItem(fromPosition, toPosition);
            }
        }
    }
}
