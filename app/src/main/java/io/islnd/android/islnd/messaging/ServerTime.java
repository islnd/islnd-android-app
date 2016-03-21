package io.islnd.android.islnd.messaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

import io.islnd.android.islnd.app.R;

public class ServerTime {
    private static final String TAG = ServerTime.class.getSimpleName();
    private static final int REPETITIONS = 2;

    public static void synchronize(Context context, boolean force) {
        final String prefKey = context.getString(R.string.server_time_offset);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        if (force || !settings.contains(prefKey)) {
            // get from server
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        long serverTimeOffset =
                                MessageLayer.getServerTimeOffsetMillis(context, REPETITIONS);
                        settings.edit()
                                .putString(prefKey, Long.toString(serverTimeOffset))
                                .apply();
                        Log.d(TAG, "ServerTime: saved new offset to prefs: " + serverTimeOffset);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        }
    }

    public static long getCurrentTimeMillis(Context context) {
        long serverOffsetMillis = retrieveOffset(context);
        return (System.currentTimeMillis() + serverOffsetMillis);
    }

    private static long retrieveOffset(Context context) {
        final String prefKey = context.getString(R.string.server_time_offset);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        if (!settings.contains(prefKey)) {
            Log.d(TAG, "ServerTime utilized while not synchronized!");
        }

        return settings.getLong(prefKey, 0);
    }
}
