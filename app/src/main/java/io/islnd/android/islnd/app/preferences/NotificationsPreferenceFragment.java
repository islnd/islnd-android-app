package io.islnd.android.islnd.app.preferences;

import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.SyncAlarm;
import io.islnd.android.islnd.app.util.Util;

public class NotificationsPreferenceFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = NotificationsPreferenceFragment.class.getSimpleName();

    public static final String PREF_SYNC_INTERVAL_KEY = "pref_sync_interval_key";
    public static final String PREF_ENABLE_NOTIFICATIONS_KEY = "pref_enable_notifications_key";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_notifications);

        Preference syncIntervalPreference = findPreference(PREF_SYNC_INTERVAL_KEY);
        syncIntervalPreference.setOnPreferenceChangeListener(this);

        String syncIntervalArrayValue = PreferenceManager
                .getDefaultSharedPreferences(getContext())
                .getString(PREF_SYNC_INTERVAL_KEY, getString(R.string.pref_sync_interval_default_value));
        setSyncIntervalSummary(syncIntervalArrayValue);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String value = o.toString();
        String key = preference.getKey();
        Log.d(TAG, "pref key: " + key);
        Log.d(TAG, "pref value: " + value);

        switch (key) {
            case PREF_SYNC_INTERVAL_KEY:
                String oldValue = PreferenceManager
                        .getDefaultSharedPreferences(getContext())
                        .getString(PREF_SYNC_INTERVAL_KEY, getString(R.string.pref_sync_interval_default_value));

                if (oldValue.equals(value)) {
                    return false;
                }

                setSyncIntervalSummary(value);
                break;
        }
        return true;
    }

    private void setSyncIntervalSummary(String arrayValue) {
        Preference syncIntervalPreference = findPreference(PREF_SYNC_INTERVAL_KEY);

        switch (arrayValue) {
            case "1":
                syncIntervalPreference.setSummary(getString(R.string.five_minutes));
                break;
            case "2":
                syncIntervalPreference.setSummary(getString(R.string.ten_minutes));
                break;
            case "3":
                syncIntervalPreference.setSummary(getString(R.string.fifteen_minutes));
                break;
            case "4":
                syncIntervalPreference.setSummary(getString(R.string.thirty_minutes));
                break;
            case "5":
                syncIntervalPreference.setSummary(getString(R.string.one_hour));
                break;
            case "6":
                syncIntervalPreference.setSummary(getString(R.string.two_hours));
                break;
            case "7":
                syncIntervalPreference.setSummary(getString(R.string.four_hours));
                break;
            case "8":
                syncIntervalPreference.setSummary(getString(R.string.twelve_hours));
                break;
            case "9":
                syncIntervalPreference.setSummary(getString(R.string.daily));
                break;
            default:
                syncIntervalPreference.setSummary(getString(R.string.thirty_minutes));
        }
    }
}
