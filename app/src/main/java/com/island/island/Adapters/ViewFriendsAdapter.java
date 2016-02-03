package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.island.island.Activities.ProfileActivity;
import com.island.island.Containers.User;
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
    public void onBindViewHolder(FriendGlanceViewHolder holder, int position)
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
    }

    @Override
    public int getItemCount()
    {
        return mList.size();
    }
}
