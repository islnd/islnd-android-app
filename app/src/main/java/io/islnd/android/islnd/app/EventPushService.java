package io.islnd.android.islnd.app;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.security.Key;

import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;
import io.islnd.android.islnd.messaging.event.Event;

public class EventPushService extends IntentService {

    private static final String TAG = EventPushService.class.getSimpleName();

    public static final String EVENT_EXTRA = "event_extra";

    public EventPushService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Event event = (Event) intent.getSerializableExtra(EVENT_EXTRA);
        Context context = getApplicationContext();
        Key privateKey = Util.getPrivateKey(context);
        Key groupKey = Util.getGroupKey(context);
        String apiKey = Util.getApiKey(context);
        EncryptedEvent encryptedEvent = new EncryptedEvent(event, privateKey, groupKey);

        int attemptCount = 0;
        while (attemptCount++ < 4) {
            try {
                Rest.postEvent(encryptedEvent, apiKey);
                Log.d(TAG, "post event complete.");
                break;
            } catch (IOException e) {
                Log.d(TAG, "post event failed.");
                e.printStackTrace();
            }

            Log.d(TAG, "retrying...");
        }
    }
}
