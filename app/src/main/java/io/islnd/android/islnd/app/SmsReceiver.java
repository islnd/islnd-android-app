package io.islnd.android.islnd.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsMessage;
import android.util.Log;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.fragments.ViewFriendsFragment;
import io.islnd.android.islnd.messaging.MessageLayer;

public class SmsReceiver extends BroadcastReceiver
{
    private static String TAG = SmsReceiver.class.getSimpleName();
    private static final String SMS_BUNDLE = "pdus";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String smsBody = "";
        SmsMessage[] messages;

        if (Build.VERSION.SDK_INT >= 19)
        {
            Log.d(TAG, "build version at least 19");
            messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        }
        else
        {
            Log.d(TAG, "build version less than 19");
            Bundle bundle = intent.getExtras();
            Object pdus[] = (Object[]) bundle.get(SMS_BUNDLE);
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; ++i)
            {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
        }

        Log.d(TAG, messages.length + " messages");
        for (int i = 0; i < messages.length; ++i)
        {
            smsBody += messages[i].getMessageBody();
        }

        if (isRelevant(context, smsBody)) {
            Log.d(TAG, smsBody);
            // TODO: Don't notify if user is already friend.
            MessageLayer.addPublicIdentityFromSms(
                    context,
                    smsBody.replace(context.getString(R.string.sms_prefix), ""));
        }
    }

    private boolean isRelevant(Context context, String smsBody)
    {
        return smsBody.startsWith(context.getString(R.string.sms_prefix));
    }
}
