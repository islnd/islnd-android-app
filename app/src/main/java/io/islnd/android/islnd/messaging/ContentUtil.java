package io.islnd.android.islnd.messaging;

import java.util.List;

import io.islnd.android.islnd.app.models.VersionedContent;

public class ContentUtil {
    private static final String TAG = ContentUtil.class.getSimpleName();

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
