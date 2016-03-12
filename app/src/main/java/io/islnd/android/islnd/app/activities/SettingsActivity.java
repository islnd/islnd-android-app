package io.islnd.android.islnd.app.activities;


import android.accounts.Account;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.preferences.AppPreferenceFragment;
import io.islnd.android.islnd.app.util.Util;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

//    private static void bindPreferenceSummaryToValue(Preference preference) {
//        // Set the listener to watch for value changes.
//        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
//
//        // Trigger the listener immediately with the preference's
//        // current value.
//        sBindPreferenceSummaryToValueListener.onPreferenceChange(
//                preference,
//                PreferenceManager
//                        .getDefaultSharedPreferences(preference.getContext())
//                        .getString(preference.getKey(), ""));
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set launching fragment
        Fragment fragment = new AppPreferenceFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    public void onPreferenceFragmentSelected(Fragment fragment) {
        Log.v(TAG, "switched to " + fragment.getClass().getSimpleName());
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }
}
