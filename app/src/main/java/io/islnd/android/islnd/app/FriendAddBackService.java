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
import io.islnd.android.islnd.messaging.Identity;
import io.islnd.android.islnd.messaging.MessageLayer;
import io.islnd.android.islnd.messaging.ProfileResource;
import io.islnd.android.islnd.messaging.Rest;
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
    public static final int IDENTITY_JOB = 0;
    public static final int PROFILE_JOB = 1;

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
            case IDENTITY_JOB: {
                Identity myIdentity = MessageLayer.getMyIdentity(this);
                Message identityMessage = MessageBuilder.buildIdentityMessage(
                        this,
                        inbox,
                        myIdentity);
                EncryptedMessage encryptedMessage = new EncryptedMessage(identityMessage, publicKey);
                Rest.postMessage(encryptedMessage, Util.getApiKey(this));
                Log.v(TAG, "sending identity w alias " + myIdentity.getAlias());
                break;
            }
            case PROFILE_JOB: {
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
                EncryptedMessage encryptedMessage = new EncryptedMessage(message, publicKey);
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
