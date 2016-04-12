package io.islnd.android.islnd.app;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.security.KeyPair;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

public class CreateIdentityService extends IntentService {

    private static final String TAG = CreateIdentityService.class.getSimpleName();

    public static final String DISPLAY_NAME_EXTRA = "display_name_extra";

    public CreateIdentityService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent");

        String displayName = intent.getStringExtra(DISPLAY_NAME_EXTRA);
        Context context = getApplicationContext();
        setKeyPairAndPostPublicKey(context);
        setGroupKey(context);
        int userId = setAlias(context, displayName);
        setDefaultProfile(context, userId, displayName);

        context.sendBroadcast(new Intent(IslndAction.CREATE_ACCOUNT_COMPLETED));
        Log.v(TAG, "onHandleIntent completed");
    }

    private void setKeyPairAndPostPublicKey(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        KeyPair keyPair = CryptoUtil.getKeyPair();
        String privateKey = CryptoUtil.encodeKey(keyPair.getPrivate());
        String publicKey = CryptoUtil.encodeKey(keyPair.getPublic());
        editor.putString(context.getString(R.string.private_key), privateKey);
        editor.putString(context.getString(R.string.public_key), publicKey);
        editor.commit();
    }

    private static void setGroupKey(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        String groupKey = CryptoUtil.encodeKey(CryptoUtil.getKey());
        editor.putString(context.getString(R.string.group_key), groupKey);
        editor.commit();
    }

    private int setAlias(Context context, String displayName) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        String alias = CryptoUtil.createAlias();
        String messageInbox = CryptoUtil.createAlias();
        DataUtils.addMailboxToQuerySet(context, messageInbox);

        long userId = DataUtils.insertUser(
                context,
                displayName,
                alias,
                messageInbox,
                null,
                Util.getGroupKey(context),
                Util.getPublicKey(context));

        editor.putInt(context.getString(R.string.user_id), (int) userId);
        editor.putString(context.getString(R.string.message_inbox), messageInbox);
        editor.commit();

        return (int)userId;
    }

    private void setDefaultProfile(Context context, int userId, String displayName) {
        Profile defaultProfile = Util.buildDefaultProfile(
                context,
                displayName);
        DataUtils.insertProfile(
                context,
                defaultProfile,
                userId);
    }
}
