package org.island.messaging;

import android.util.Log;

import com.island.island.Models.VersionedContent;

import java.util.List;

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static long getPostTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    public static <T extends VersionedContent> T getNewest(List<T> versionedContent) {
        T newestItem = null;
        int newestVersion = Integer.MIN_VALUE;

        for (T contentItem : versionedContent) {
            if (contentItem.getVersion() > newestVersion) {
                newestItem = contentItem;
                newestVersion = contentItem.getVersion();
            }
        }

        return newestItem;
    }
}
