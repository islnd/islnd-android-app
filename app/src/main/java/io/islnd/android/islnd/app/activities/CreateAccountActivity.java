package io.islnd.android.islnd.app.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;

import io.islnd.android.islnd.app.CreateIdentityService;
import io.islnd.android.islnd.app.IslndAction;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Rest;

public class CreateAccountActivity extends IslndActivity {

    private static final String TAG = CreateAccountActivity.class.getSimpleName();
    private Context mContext;
    private TextInputEditText mApiEditText;
    private TextInputEditText mDisplayNameEditText;
    private TextInputLayout mApiEditTextLayout;
    private String mApiKey;
    private ProgressDialog mProgressDialog;
    private BroadcastReceiver mAccountCreatedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mContext = getApplicationContext();
        mAccountCreatedListener = new AccountCreatedListener();
        mDisplayNameEditText = (TextInputEditText) findViewById(R.id.display_name_edit_text);
        mApiEditText = (TextInputEditText) findViewById(R.id.api_key_edit_text);

        if (Util.getUsesApiKey(mContext)) {
            mApiEditTextLayout = (TextInputLayout) findViewById(R.id.api_key_edit_text_layout);
            mApiEditTextLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mContext.registerReceiver(
                mAccountCreatedListener,
                new IntentFilter(IslndAction.CREATE_ACCOUNT_COMPLETED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mContext.unregisterReceiver(mAccountCreatedListener);
    }

    private void createAccount() {
        String displayName = mDisplayNameEditText.getText().toString();

        if (Util.getUsesApiKey(mContext)) {
            Util.setApiKey(mContext, mApiKey);
        }

        Intent createIdentityIntent = new Intent(this, CreateIdentityService.class);
        createIdentityIntent.putExtra(CreateIdentityService.DISPLAY_NAME_EXTRA, displayName);
        mProgressDialog.setMessage(getString(R.string.creating_identity_message));
        startService(createIdentityIntent);
    }

    public void createAccountClick(View view) {
        if (Util.getUsesApiKey(mContext)) {
            mApiKey = mApiEditText.getText().toString();
            showVerifyProgressDialog();
            new VerifyApiKeyAndCreateAccountTask().execute();
        } else {
            createAccount();
        }
    }

    private void showVerifyProgressDialog() {
        mProgressDialog = new ProgressDialog(this, R.style.AppTheme_Dialog);
        mProgressDialog.setTitle(getString(R.string.please_wait_title));
        mProgressDialog.setMessage(getString(R.string.verifying_api_key_message));
        mProgressDialog.show();
    }

    private class VerifyApiKeyAndCreateAccountTask extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void... params) {
            return Rest.getPing(mApiKey);
        }

        protected void onPostExecute(Boolean isApiKeyValid) {
            if (isApiKeyValid) {
                Log.v(TAG, "api key is valid");
                createAccount();
            } else {
                Log.v(TAG, "api key is invalid");
                mProgressDialog.dismiss();
                if (Util.getUsesApiKey(mContext)) {
                    mApiEditTextLayout.setError(getString(R.string.invalid_api_key));
                }
            }
        }
    }

    private class AccountCreatedListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mProgressDialog.dismiss();
            Util.setHasCreatedAccount(mContext, true);
            Util.enableIncrementalSyncs(mContext);
            finish();
            startActivity(new Intent(mContext, NavBaseActivity.class));
        }
    }
}
