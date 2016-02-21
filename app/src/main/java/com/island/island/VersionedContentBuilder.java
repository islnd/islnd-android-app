package com.island.island;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.island.island.Activities.NewPostActivity;
import com.island.island.Models.Post;
import com.island.island.Models.Profile;

import org.island.messaging.PostUpdate;

public class VersionedContentBuilder {
    private static final String TAG = VersionedContentBuilder.class.getSimpleName();

    public static Profile buildProfile(Context context, String username, String string) {
        int newVersion = getNewVersionAndUpdate(
                context,
                context.getString(R.string.previous_profile_version_key));
        return new Profile(username, string, newVersion);
    }

    public static PostUpdate buildPost(Context context, String content) {
        int newVersion = getNewVersionAndUpdate(
                context,
                context.getString(R.string.post_version_key));
        return PostUpdate.buildPost(content, String.valueOf(newVersion));
    }

    private static int getNewVersionAndUpdate(Context context, String versionKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int lastVersion = preferences.getInt(
                versionKey,
                0);
        int newVersion = lastVersion + 1;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(versionKey, newVersion);
        editor.commit();
        return newVersion;
    }
}