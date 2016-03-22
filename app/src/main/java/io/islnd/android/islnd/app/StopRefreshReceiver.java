package io.islnd.android.islnd.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;

public class StopRefreshReceiver extends BroadcastReceiver {
    private final SwipeRefreshLayout mRefreshLayout;

    public StopRefreshReceiver(SwipeRefreshLayout refreshLayout) {
        mRefreshLayout = refreshLayout;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mRefreshLayout.setRefreshing(false);
    }
}
