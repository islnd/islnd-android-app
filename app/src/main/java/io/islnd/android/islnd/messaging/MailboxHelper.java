package io.islnd.android.islnd.messaging;

import android.content.Context;
import android.util.Log;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

public class MailboxHelper {

    private static final String TAG = MailboxHelper.class.getSimpleName();

    public static String getAndSetMyNewInbox(Context context) {
        String newInbox = CryptoUtil.getNewMailbox();
        DataUtils.updateMyUserMailbox(context, newInbox);
        Log.v(TAG, "my new inbox is " + newInbox);
        return newInbox;
    }
}
