package io.islnd.android.islnd.app.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.viewholders.NotificationViewHolder;

public class NotificationAdapter extends CursorRecyclerViewAdapter<NotificationViewHolder> {

    public NotificationAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder viewHolder, Cursor cursor) {

    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification, parent, false);

        return new NotificationViewHolder(v);
    }
}
