package io.islnd.android.islnd.app.preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.util.Util;

public class AccountPreferenceFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceClickListener{

    private static final String TAG = AccountPreferenceFragment.class.getSimpleName();

    public static final String PREFERENCE_DELETE_ACCOUNT_KEY = "pref_delete_account_from_device_key";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_account);

        Preference deleteAccountPreference = findPreference(PREFERENCE_DELETE_ACCOUNT_KEY);
        deleteAccountPreference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        Log.v(TAG, "pref key: " + key);

        switch (key) {
            case PREFERENCE_DELETE_ACCOUNT_KEY:
                showDeleteAccountDialog();
                break;
        }
        return true;
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        builder.setTitle(getString(R.string.delete_account_title))
                .setMessage(getString(R.string.delete_account_message))
                .setPositiveButton(getString(R.string.delete_button), (DialogInterface dialog, int id) ->
                {
                    Util.deleteDataAndPreferences(getContext());
                    Util.restartApp((AppCompatActivity) getContext());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
