package io.islnd.android.islnd.messaging;

import android.content.Context;

import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.models.VersionedContent;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.util.ImageUtil;

import java.util.List;

public class Util {
    private static final String TAG = Util.class.getSimpleName();

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
