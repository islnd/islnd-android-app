package io.islnd.android.islnd.app;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Encoder;
import io.islnd.android.islnd.messaging.Identity;
import io.islnd.android.islnd.messaging.MessageLayer;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.message.Message;

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

        Identity myIdentity = MessageLayer.getMyIdentity(this);
        Message identityMessage = new Message(
                inbox,
                new Encoder().encodeToString(myIdentity.toByteArray()));

        Rest.postMessage(identityMessage, Util.getApiKey(this));

        Log.v(TAG, "onHandleIntent complete");
    }
}
