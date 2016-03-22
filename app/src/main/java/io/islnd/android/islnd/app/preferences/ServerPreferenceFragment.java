package io.islnd.android.islnd.app.preferences;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import io.islnd.android.islnd.app.R;

public class ServerPreferenceFragment extends PreferenceFragmentCompat {

    public static final String PREFERENCE_API_KEY_KEY = "pref_api_key_key";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_server);
    }
}
