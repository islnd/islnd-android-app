package com.island.island;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import org.island.messaging.MessageLayer;

/**
 * Created by poo on 2/18/2016.
 */
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
            messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        }
        else
        {
            Bundle bundle = intent.getExtras();
            Object pdus[] = (Object[]) bundle.get(SMS_BUNDLE);
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; ++i)
            {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
        }

        for (int i = 0; i < messages.length; ++i)
        {
            smsBody += messages[i].getMessageBody();
        }

        Log.d(TAG, smsBody);
        if (isRelevant(context, smsBody))
        {
            MessageLayer.addFriendFromEncodedString(context,
                    smsBody.replace(context.getString(R.string.sms_prefix), ""));
        }
    }

    private boolean isRelevant(Context context, String smsBody)
    {
        return smsBody.startsWith(context.getString(R.string.sms_prefix));
    }
}
