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
        System.out.println("MADE IT");
        String smsMessageStr = "";
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
            String smsBody = messages[i].getMessageBody();
            String address = messages[i].getOriginatingAddress();

            smsMessageStr += "SMS From: " + address + "\n";
            smsMessageStr += smsBody + "\n";
        }

        Log.v(TAG, smsMessageStr);

        // Start add friend intent
    }
}
