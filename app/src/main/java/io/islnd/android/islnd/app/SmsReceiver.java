package io.islnd.android.islnd.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.sms.MultipartMessage;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.MessageLayer;

public class SmsReceiver extends BroadcastReceiver
{
    private static String TAG = SmsReceiver.class.getSimpleName();
    private static final String SMS_BUNDLE = "pdus";

    @Override
    public void onReceive(Context context, Intent intent)
    {
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
            processMessage(context, messages[i]);
        }
    }

    private void processMessage(Context context, SmsMessage smsMessage) {
        if (!MultipartMessage.isIslndMessage(smsMessage)) {
            return;
        }

        MultipartMessage.save(context, smsMessage);
        if (MultipartMessage.isComplete(context, smsMessage)) {
            MessageLayer.addPublicIdentityFromSms(
                    context,
                    MultipartMessage.getComplete(context, smsMessage));
        }
    }
}
