package io.islnd.android.islnd.app;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.media.session.IMediaControllerCallback;
import android.util.Log;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Encoder;
import io.islnd.android.islnd.messaging.Identity;
import io.islnd.android.islnd.messaging.MessageLayer;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.message.Message;
import io.islnd.android.islnd.messaging.message.MessageBuilder;
import io.islnd.android.islnd.messaging.message.MessageType;
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
        Log.v(TAG, "add back to mailbox " + inbox + " job " + job);
        switch (job) {
            case IDENTITY_JOB: {
                Identity myIdentity = MessageLayer.getMyIdentity(this);
                Message identityMessage = MessageBuilder.buildIdentityMessage(
                        inbox,
                        myIdentity);
                Rest.postMessage(identityMessage, Util.getApiKey(this));
                break;
            }
            case PROFILE_JOB: {
                Profile profile = DataUtils.getProfile(this, IslndContract.UserEntry.MY_USER_ID);
                ProfileMessage myProfile = new ProfileMessage(
                        profile.getAboutMe(),
                        ImageUtil.getByteArrayFromUri(this, profile.getProfileImageUri()),
                        ImageUtil.getByteArrayFromUri(this, profile.getHeaderImageUri()));
                Message profileMessage = MessageBuilder.buildProfileMessage(
                        inbox,
                        myProfile);
                Rest.postMessage(profileMessage, Util.getApiKey(this));
                break;
            }
            default: {
                throw new UnsupportedOperationException("don't recognize job " + job);
            }
        }

        Log.v(TAG, "onHandleIntent complete");
    }
}