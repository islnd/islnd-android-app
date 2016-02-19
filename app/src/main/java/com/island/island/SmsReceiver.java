package com.island.island;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import com.island.island.Activities.ViewFriendsActivity;

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

        if (isRelevant(context, smsBody))
        {
            Log.d(TAG, smsBody);
            // TODO: Don't notify if user is already friend.
            MessageLayer.addFriendFromEncodedString(context,
                    smsBody.replace(context.getString(R.string.sms_prefix), ""));
            newFriendNotification(context);
        }
    }

    private boolean isRelevant(Context context, String smsBody)
    {
        return smsBody.startsWith(context.getString(R.string.sms_prefix));
    }

    // TODO: Pass in user name
    private void newFriendNotification(Context context)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.new_friend_notification))
                        .setContentText("A friend allowed you via SMS!");

        Intent resultIntent = new Intent(context, ViewFriendsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ViewFriendsActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(context.getResources().
                getInteger(R.integer.notif_sms_friend_added), mBuilder.build());
    }
}
