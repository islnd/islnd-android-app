package io.islnd.android.islnd.app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.IslndDb;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.MessageLayer;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = CreateAccountActivity.class.getSimpleName();
    private Context mContext;
    private TextInputEditText mApiEditText;
    private TextInputEditText mDisplayNameEditText;
    private TextInputLayout mApiEditTextLayout;
    private String mApiKey;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mContext = getApplicationContext();
        mDisplayNameEditText = (TextInputEditText) findViewById(R.id.display_name_edit_text);
        mApiEditText = (TextInputEditText) findViewById(R.id.api_key_edit_text);

        if (Util.getUsesApiKey(mContext)) {
            mApiEditTextLayout = (TextInputLayout) findViewById(R.id.api_key_edit_text_layout);
            mApiEditTextLayout.setVisibility(View.VISIBLE);
        }
    }

    private void createAccount() {
        String displayName = mDisplayNameEditText.getText().toString();

        if (Util.getUsesApiKey(mContext)) {
            Util.setApiKey(mContext, mApiKey);
        }
        IslndDb.createIdentity(mContext, displayName);
        Util.setHasCreatedAccount(mContext, true);

        finish();
        startActivity(new Intent(this, NavBaseActivity.class));
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
            return MessageLayer.getPing(mApiKey);
        }

        protected void onPostExecute(Boolean isApiKeyValid) {
            if (isApiKeyValid) {
                Log.v(TAG, "api key is valid");
                createAccount();
            } else {
                Log.v(TAG, "api key is invalid");
                if (Util.getUsesApiKey(mContext)) {
                    mApiEditTextLayout.setError(getString(R.string.invalid_api_key));
                }
            }
            mProgressDialog.dismiss();
        }
    }
}
