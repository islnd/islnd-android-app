package io.islnd.android.islnd.messaging;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

public class ServerTime {
    private static final String TAG = ServerTime.class.getSimpleName();
    private static final int REPETITIONS = 2;

    private static long serverOffsetMillis = 0;
    private static boolean inSync = false;
    public static boolean isInSync() {
        return inSync;
    }

    public static long synchronize(Context context) throws IOException {
        serverOffsetMillis = MessageLayer.getServerTimeOffsetMillis(context, REPETITIONS);
        inSync = true;
        return serverOffsetMillis;
    }

    public static void synchronizeManually(long newServerOffsetMillis) {
        serverOffsetMillis = newServerOffsetMillis;
        inSync = true;
    }

    public static long toServerTimeMillis(long localTimeMillis) {
        warnIfNotInSync();
        return (localTimeMillis + serverOffsetMillis);
    }

    public static long toLocalTimeMillis(long serverTimeMillis) {
        warnIfNotInSync();
        return (serverTimeMillis - serverOffsetMillis);
    }

    public static long getCurrentTimeMillis() {
        return toServerTimeMillis(System.currentTimeMillis());
    }

    private static void warnIfNotInSync() {
        if (!inSync) {
            Log.d(TAG, "ServerTime utilized while not synchronized!");
        }
    }
}
