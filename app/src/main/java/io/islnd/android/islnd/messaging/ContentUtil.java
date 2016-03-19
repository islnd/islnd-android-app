package io.islnd.android.islnd.messaging;

import io.islnd.android.islnd.app.models.VersionedContent;

import java.util.List;

public class ContentUtil {
    private static final String TAG = ContentUtil.class.getSimpleName();

    public static long getContentTimestamp() {
        return System.currentTimeMillis();
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
