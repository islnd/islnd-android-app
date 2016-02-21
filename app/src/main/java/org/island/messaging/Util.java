package org.island.messaging;

import android.content.Context;
import android.util.Log;

import com.island.island.Models.Profile;
import com.island.island.Models.VersionedContent;
import com.island.island.R;

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
        return new Profile(
                username,
                context.getString(R.string.profile_default_about_me),
                Integer.MIN_VALUE);
    }
}
