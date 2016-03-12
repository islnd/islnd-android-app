package io.islnd.android.islnd.app.preferences;

import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import io.islnd.android.islnd.app.R;

public class ThemePreferenceFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = ThemePreferenceFragment.class.getSimpleName();

    public static final String PREFERENCE_THEME_KEY = "pref_theme_key";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_theme);

        Preference themePreference = findPreference(PREFERENCE_THEME_KEY);
        themePreference.setOnPreferenceChangeListener(this);
        setThemeSummary(themePreference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String value = o.toString();
        String key = preference.getKey();
        Log.v(TAG, "pref key: " + key);
        Log.v(TAG, "pref value: " + value);

        switch (key) {
            case PREFERENCE_THEME_KEY:
                switch (value) {
                    case "1": // Light
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        preference.setSummary(getString(R.string.light_theme));
                        break;
                    case "2": // Dark
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        preference.setSummary(getString(R.string.dark_theme));
                        break;
                    case "3": // DayNight Auto
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                        preference.setSummary(getString(R.string.day_night_theme));
                        break;
                }
                break;
        }
        return true;
    }

    private void setThemeSummary(Preference preference) {
        String value = PreferenceManager
                .getDefaultSharedPreferences(getContext())
                .getString(PREFERENCE_THEME_KEY, "1");

        switch (value) {
            case "1": // Light
                preference.setSummary(getString(R.string.light_theme));
                break;
            case "2": // Dark
                preference.setSummary(getString(R.string.dark_theme));
                break;
            case "3": // DayNight Auto
                preference.setSummary(getString(R.string.day_night_theme));
                break;
        }
    }
}
