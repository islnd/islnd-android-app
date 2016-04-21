package io.islnd.android.islnd.app.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import io.islnd.android.islnd.app.R;

public class InviteViewHolder extends RecyclerView.ViewHolder  {

    public TextView inviteMessage;
    public View view;

    public InviteViewHolder(View itemView) {
        super(itemView);
        inviteMessage = (TextView) itemView.findViewById(R.id.invite_message);
        view = itemView;
    }
}
