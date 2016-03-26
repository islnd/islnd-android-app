package io.islnd.android.islnd.app.preferences;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import io.islnd.android.islnd.app.R;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private static final String FRAGMENT_STATE = "FRAGMENT_STATE";

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_STATE);
        } else {
            mFragment = new AppPreferenceFragment();
        }
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mFragment)
                .commit();
    }

    public void onPreferenceFragmentSelected(Fragment fragment) {
        Log.v(TAG, "switched to " + fragment.getClass().getSimpleName());
        mFragment = fragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mFragment)
                .addToBackStack("")
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        getSupportFragmentManager().putFragment(savedInstanceState, FRAGMENT_STATE, mFragment);
        super.onSaveInstanceState(savedInstanceState);
    }
}
