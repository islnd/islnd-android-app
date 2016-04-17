package io.islnd.android.islnd.app;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.security.Key;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.PublicIdentity;
import io.islnd.android.islnd.messaging.MessageLayer;
import io.islnd.android.islnd.messaging.ProfileResource;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.SecretIdentity;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.crypto.EncryptedMessage;
import io.islnd.android.islnd.messaging.crypto.EncryptedResource;
import io.islnd.android.islnd.messaging.message.Message;
import io.islnd.android.islnd.messaging.message.MessageBuilder;
import io.islnd.android.islnd.messaging.message.ProfileMessage;

public class FriendAddBackService extends IntentService {

    private static final String TAG = FriendAddBackService.class.getSimpleName();

    public static final String MAILBOX_EXTRA = "mailbox_extra";
    public static final String JOB_EXTRA = "job_extra";
    public static final int PUBLIC_IDENTITY_JOB = 0;
    public static final int SECRET_IDENTITY_JOB = 1;
    public static final int PROFILE_JOB = 2;

    public FriendAddBackService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent");

        String inbox = intent.getStringExtra(MAILBOX_EXTRA);

        int job = intent.getIntExtra(JOB_EXTRA, -1);
        Key publicKey = DataUtils.getPublicKeyForUserInbox(this, inbox);
        Log.v(TAG, "add back to mailbox " + inbox + " job " + job);
        switch (job) {
            case PUBLIC_IDENTITY_JOB: {
                Log.d(TAG, "public identity job");
                PublicIdentity myPublicIdentity = MessageLayer.getMyPublicIdentity(this);
                Message identityMessage = MessageBuilder.buildPublicIdentityMessage(
                        this,
                        inbox,
                        myPublicIdentity);
                EncryptedMessage encryptedMessage = new EncryptedMessage(
                        identityMessage,
                        publicKey,
                        Util.getPrivateKey(this));
                Rest.postMessage(encryptedMessage, Util.getApiKey(this));
                break;
            }
            case SECRET_IDENTITY_JOB: {
                Log.d(TAG, "secret identity job");
                SecretIdentity mySecretIdentity = MessageLayer.getMySecretIdentity(this);
                Message identityMessage = MessageBuilder.buildSecretIdentityMessage(
                        this,
                        inbox,
                        mySecretIdentity);
                EncryptedMessage encryptedMessage = new EncryptedMessage(
                        identityMessage,
                        publicKey,
                        Util.getPrivateKey(this));
                Rest.postMessage(encryptedMessage, Util.getApiKey(this));
                break;
            }
            case PROFILE_JOB: {
                Log.d(TAG, "profile job");
                Profile profile = DataUtils.getProfile(this, IslndContract.UserEntry.MY_USER_ID);
                ProfileResource profileResource = new ProfileResource(
                        profile.getAboutMe(),
                        ImageUtil.getScaledImageByteArrayFromUri(this, profile.getProfileImageUri()),
                        ImageUtil.getScaledImageByteArrayFromUri(this, profile.getHeaderImageUri()));
                String profileResourceKey = CryptoUtil.createAlias();
                EncryptedResource encryptedResource = new EncryptedResource(
                        profileResource,
                        Util.getPrivateKey(this),
                        Util.getGroupKey(this),
                        profileResourceKey
                );
                Rest.postResource(
                        encryptedResource,
                        Util.getApiKey(this)
                );

                ProfileMessage profileMessage = new ProfileMessage(
                        profileResourceKey
                );
                Message message = MessageBuilder.buildProfileMessage(
                        this,
                        inbox,
                        profileMessage);
                EncryptedMessage encryptedMessage = new EncryptedMessage(
                        message,
                        publicKey,
                        Util.getPrivateKey(this));
                Rest.postMessage(encryptedMessage, Util.getApiKey(this));

                break;
            }
            default: {
                throw new UnsupportedOperationException("don't recognize job " + job);
            }
        }

        Log.v(TAG, "onHandleIntent complete");
    }
}
