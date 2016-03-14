package io.islnd.android.islnd.app.preferences;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import io.islnd.android.islnd.app.R;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set launching fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new AppPreferenceFragment())
                .commit();
    }

    public void onPreferenceFragmentSelected(Fragment fragment) {
        Log.v(TAG, "switched to " + fragment.getClass().getSimpleName());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack("")
                .commit();
    }
}
