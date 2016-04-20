package io.islnd.android.islnd.app.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.util.Util;

public class SplashScreenActivity extends IslndActivity {

    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mContext = getApplicationContext();

        // Create sync account and force sync
        Account syncAccount = createAndRegisterSyncAccount(this);
        ContentResolver resolver = getContentResolver();
        resolver.setSyncAutomatically(
                syncAccount,
                getString(R.string.content_authority),
                true);

        Log.d(TAG, "requestSync");
        resolver.requestSync(syncAccount, IslndContract.CONTENT_AUTHORITY, new Bundle());

        Util.setUsesApiKey(mContext, true);

        // Visual pause...
        Handler handler = new Handler();
        Runnable runnable = () -> {
            finish();

            if (Util.getHasCreatedAccount(mContext)) {
                startActivity(new Intent(SplashScreenActivity.this, NavBaseActivity.class));
            } else {
                startActivity(new Intent(SplashScreenActivity.this, CreateAccountActivity.class));
            }
        };

        handler.postDelayed(runnable, 1500);
    }

    public static Account createAndRegisterSyncAccount(Context context) {
        Account newAccount = Util.getSyncAccount(context);

        //--Register account
        AccountManager accountManager = (AccountManager) context.getSystemService(context.ACCOUNT_SERVICE);
        accountManager.addAccountExplicitly(newAccount, null, null);

        return newAccount;
    }
}
