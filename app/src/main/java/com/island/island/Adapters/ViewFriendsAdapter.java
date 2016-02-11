package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.island.island.Activities.ProfileActivity;
import com.island.island.Dialogs;
import com.island.island.Models.User;
import com.island.island.R;
import com.island.island.ViewHolders.FriendGlanceViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by poo on 2/3/2016.
 */
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
        final User user = mList.get(position);

        holder.userName.setText(user.getUserName());

        // Go to profile on view click
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, user.getUserName());
                mContext.startActivity(profileIntent);
            }
        });

        // Remove friend
        holder.removeFriend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Dialogs.removeFriendDialog(mContext, user.getUserName());
                // TODO: Remove friend from list?
            }
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
