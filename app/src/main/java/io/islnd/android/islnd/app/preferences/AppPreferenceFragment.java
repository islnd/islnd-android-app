package io.islnd.android.islnd.app.preferences;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import io.islnd.android.islnd.app.R;

public class AppPreferenceFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceClickListener {

    private static final String TAG = AppPreferenceFragment.class.getSimpleName();

    private static final String PREFERENCE_APPEARANCE_KEY = "pref_appearance_key";
    private static final String PREFERENCE_SERVER_KEY = "pref_server_key";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

        Preference appearancePreference = findPreference(PREFERENCE_APPEARANCE_KEY);
        Preference serverPreference = findPreference(PREFERENCE_SERVER_KEY);
        appearancePreference.setOnPreferenceClickListener(this);
        serverPreference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        SettingsActivity settingsActivity = (SettingsActivity) getActivity();
        String key = preference.getKey();
        Log.v(TAG, "pref key: " + key);

        switch (key) {
            case PREFERENCE_APPEARANCE_KEY:
                settingsActivity.onPreferenceFragmentSelected(new ThemePreferenceFragment());
                break;
            case PREFERENCE_SERVER_KEY:
                settingsActivity.onPreferenceFragmentSelected(new ServerPreferenceFragment());
                break;
        }
        return true;
    }
}
