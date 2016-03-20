package io.islnd.android.islnd.app.activities;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.IslndDb;
import io.islnd.android.islnd.app.util.Util;

public class CreateAccountActivity extends AppCompatActivity {

    private Context mContext;
    private TextInputEditText mApiEditText;
    private TextInputEditText mDisplayNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mContext = getApplicationContext();
        mDisplayNameEditText = (TextInputEditText) findViewById(R.id.display_name_edit_text);
        mApiEditText = (TextInputEditText) findViewById(R.id.api_key_edit_text);

        if (Util.getUsesApiKey(mContext)) {
            TextInputLayout apiEditTextLayout =
                    (TextInputLayout) findViewById(R.id.api_key_edit_text_layout);
            apiEditTextLayout.setVisibility(View.VISIBLE);
        }
    }

    public void createAccount(View view) {
        Util.setApiKey(mContext, mApiEditText.getText().toString());
        IslndDb.createIdentity(mContext, mDisplayNameEditText.getText().toString());
        Util.setHasCreatedAccount(mContext, true);

        finish();
        startActivity(new Intent(this, NavBaseActivity.class));
    }
}
