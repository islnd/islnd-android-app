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
import io.islnd.android.islnd.messaging.message.MessageType;
import io.islnd.android.islnd.messaging.message.ProfileMessage;

public class FriendAddBackService extends IntentService {

    private static final String TAG = FriendAddBackService.class.getSimpleName();

    public static final String MAILBOX_EXTRA = "mailbox_extra";

    public FriendAddBackService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent");

        String inbox = intent.getStringExtra(MAILBOX_EXTRA);
        Log.v(TAG, "add back to mailbox " + inbox);

        Identity myIdentity = MessageLayer.getMyIdentity(this);
        Message identityMessage = new Message(
                inbox,
                MessageType.IDENTITY,
                new Encoder().encodeToString(myIdentity.toByteArray()));
        Rest.postMessage(identityMessage, Util.getApiKey(this));

        Profile profile = DataUtils.getProfile(this, IslndContract.UserEntry.MY_USER_ID);
        ProfileMessage myProfile = new ProfileMessage(
                profile.getAboutMe(),
                ImageUtil.getByteArrayFromUri(this, profile.getProfileImageUri()),
                ImageUtil.getByteArrayFromUri(this, profile.getHeaderImageUri()));
        Message profileMessage = new Message(
                inbox,
                MessageType.PROFILE,
                new Encoder().encodeToString(myProfile.toByteArray()));
        Rest.postMessage(profileMessage, Util.getApiKey(this));

        Log.v(TAG, "onHandleIntent complete");
    }
}
