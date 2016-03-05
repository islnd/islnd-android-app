package io.islnd.android.islnd.messaging;

import android.content.Context;

import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.models.VersionedContent;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.util.ImageUtil;

import java.util.List;

public class Util {
    private static final String TAG = Util.class.getSimpleName();

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

    public static Profile buildDefaultProfile(Context context, String username) {
        // TODO: default image Uris will probably be assets...
        return new Profile(
                username,
                context.getString(R.string.profile_default_about_me),
                ImageUtil.getDefaultProfileImageUri(context),
                ImageUtil.getDefaultHeaderImageUri(context),
                Integer.MIN_VALUE);
    }
}
