package com.island.island;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.island.island.Models.Profile;

public class VersionedContentBuilder {
    private static final String TAG = VersionedContentBuilder.class.getSimpleName();

    public static Profile buildProfile(Context context, String username, String string) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int lastVersion = preferences.getInt(
                context.getString(R.string.previous_profile_version_key),
                0);
        int newVersion = lastVersion + 1;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.previous_profile_version_key), newVersion);
        editor.commit();

        Log.v(TAG, "built profile version " + newVersion);
        return new Profile(username, string, newVersion);
    }
}
