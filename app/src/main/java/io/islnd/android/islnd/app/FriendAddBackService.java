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

public class FriendAddBackService extends IntentService {

    private static final String TAG = FriendAddBackService.class.getSimpleName();

    public static final String MAILBOX_EXTRA = "mailbox_extra";
    public static final String JOB_EXTRA = "job_extra";
    public static final String NONCE_EXTRA = "nonce_extra";

    public static final int PUBLIC_IDENTITY_JOB = 0;
    public static final int SECRET_IDENTITY_JOB = 1;
    public static final int PROFILE_JOB = 2;

    public FriendAddBackService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        String inbox = intent.getStringExtra(MAILBOX_EXTRA);
        String nonce = intent.getStringExtra(NONCE_EXTRA);
        int job = intent.getIntExtra(JOB_EXTRA, -1);

        Key recipientPublicKey = DataUtils.getPublicKeyForUserInbox(this, inbox);
        Log.v(TAG, "add back to mailbox " + inbox + " job " + job);
        switch (job) {
            case PUBLIC_IDENTITY_JOB: {
                Log.d(TAG, "public identity job");
                sendPublicIdentityMessage(inbox, nonce, recipientPublicKey);
                break;
            }
            case SECRET_IDENTITY_JOB: {
                Log.d(TAG, "secret identity job");
                sendSecretIdentityMessage(inbox, recipientPublicKey);
                break;
            }
            case PROFILE_JOB: {
                Log.d(TAG, "profile job");
                sendProfileMessage(inbox, nonce, recipientPublicKey);
                break;
            }
            default: {
                throw new UnsupportedOperationException("don't recognize job " + job);
            }
        }

        Log.v(TAG, "onHandleIntent complete");
    }

    private void sendPublicIdentityMessage(String mailbox, String nonce, Key recipientPublicKey) {
        PublicIdentity myPublicIdentity = MessageLayer.createNewPublicIdentity(this);

        //--set my public identity inbox as the outbox for user with public key
        DataUtils.updateUserOutbox(this, myPublicIdentity.getMessageInbox(), recipientPublicKey);

        Message identityMessage = MessageBuilder.buildPublicIdentityMessage(
                this,
                mailbox,
                nonce,
                myPublicIdentity);
        EncryptedMessage encryptedMessage = new EncryptedMessage(
                identityMessage,
                recipientPublicKey,
                Util.getPrivateKey(this));
        Rest.postMessage(encryptedMessage, Util.getApiKey(this));
    }

    private void sendSecretIdentityMessage(String mailbox, Key recipientPublicKey) {
        SecretIdentity mySecretIdentity = MessageLayer.getMySecretIdentity(this);
        Message identityMessage = MessageBuilder.buildSecretIdentityMessage(
                this,
                mailbox,
                mySecretIdentity);
        EncryptedMessage encryptedMessage = new EncryptedMessage(
                identityMessage,
                recipientPublicKey,
                Util.getPrivateKey(this));
        Rest.postMessage(encryptedMessage, Util.getApiKey(this));
    }

    private void sendProfileMessage(String mailbox, String nonce, Key recipientPublicKey) {
        //--TODO don't post profile resource if my profile has not changed
        String profileResourceKey = postProfileResource();

        postProfileMessage(mailbox, recipientPublicKey, profileResourceKey);
    }

    private void postProfileMessage(String mailbox, Key recipientPublicKey, String profileResourceKey) {
        Message message = MessageBuilder.buildProfileMessage(
                this,
                mailbox,
                profileResourceKey);
        EncryptedMessage encryptedMessage = new EncryptedMessage(
                message,
                recipientPublicKey,
                Util.getPrivateKey(this));
        Rest.postMessage(encryptedMessage, Util.getApiKey(this));
    }

    private String postProfileResource() {
        Profile myProfile = DataUtils.getProfile(this, IslndContract.UserEntry.MY_USER_ID);
        ProfileResource profileResource = new ProfileResource(
                myProfile.getAboutMe(),
                ImageUtil.getScaledImageByteArrayFromUri(this, myProfile.getProfileImageUri()),
                ImageUtil.getScaledImageByteArrayFromUri(this, myProfile.getHeaderImageUri()));
        String profileResourceKey = CryptoUtil.getNewResourceKey();
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

        return profileResourceKey;
    }
}
