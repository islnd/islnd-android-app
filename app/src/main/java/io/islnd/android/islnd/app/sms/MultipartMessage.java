package io.islnd.android.islnd.app.sms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

public class MultipartMessage {

    private static final String TAG = MultipartMessage.class.getSimpleName();

    public static final int MAX_LENGTH = 125;
    public static final String ISLND_SMS_PREFIX = "ISLND$$$";

    public static final int HEADER_LENGTH = ISLND_SMS_PREFIX.length() + 13; //HEADER!ddddd!dd!dd!
    private static final String DELIMITER = "!";

    public static List<String> buildMessages(String message) {
        final int contentLength = MAX_LENGTH - HEADER_LENGTH - ISLND_SMS_PREFIX.length();
        final int numberOfMessages = (int)Math.ceil(message.length() * 1.0 / contentLength);
        final int messageId = generateMessageId();
        List<String> messages = new ArrayList();
        for (int i = 0; i < numberOfMessages; i++) {
            String header = buildHeader(messageId, i, numberOfMessages);
            int startIndex = i * contentLength;

            String messagePart;
            if (i == numberOfMessages - 1) {
                messagePart = message.substring(startIndex);
            } else {
                int endIndex = startIndex + contentLength;
                messagePart = message.substring(startIndex, endIndex);
            }

            messages.add(header + messagePart);
        }

        return messages;
    }

    private static int generateMessageId() {
        return CryptoUtil.generateSmsMessageId();
    }

    private static String buildHeader(int messageId, int messagePartId, int numberOfMessages) {
        return ISLND_SMS_PREFIX +
                DELIMITER +
                messageId +
                DELIMITER +
                messagePartId +
                DELIMITER +
                (numberOfMessages - 1) +
                DELIMITER;
    }

    public static boolean isIslndMessage(SmsMessage smsMessage)
    {
        return smsMessage.getMessageBody().startsWith(MultipartMessage.ISLND_SMS_PREFIX);
    }

    public static void save(Context context, SmsMessage smsMessage) {
        IslndMessagePart messagePart = buildMessagePart(smsMessage);
        Log.v(TAG, String.format("save message %s part %d of %d",
                messagePart.getMessageId(),
                messagePart.getMessagePartId(),
                messagePart.getLastMessagePartId()));
        ContentValues values = new ContentValues();
        values.put(IslndContract.SmsMessageEntry.COLUMN_BODY, messagePart.getBody());
        values.put(IslndContract.SmsMessageEntry.COLUMN_MESSAGE_ID, messagePart.getMessageId());
        values.put(IslndContract.SmsMessageEntry.COLUMN_MESSAGE_PART_ID, messagePart.getMessagePartId());
        values.put(IslndContract.SmsMessageEntry.COLUMN_LAST_MESSAGE_PART_ID, messagePart.getLastMessagePartId());
        values.put(IslndContract.SmsMessageEntry.COLUMN_ORIGINATING_ADDRESS, messagePart.getOriginatingAddress());
        context.getContentResolver().insert(
                IslndContract.SmsMessageEntry.CONTENT_URI,
                values);
    }

    private static IslndMessagePart buildMessagePart(SmsMessage smsMessage) {
        String[] tokens = smsMessage.getMessageBody().split(MultipartMessage.DELIMITER);
        return new IslndMessagePart(
                smsMessage.getOriginatingAddress(),
                tokens[1],
                Integer.parseInt(tokens[2]),
                Integer.parseInt(tokens[3]),
                tokens[4]);
    }

    public static boolean isComplete(Context context, SmsMessage smsMessage) {
        Log.d(TAG, "isComplete");
        List<IslndMessagePart> parts = getMessageParts(context, smsMessage);
        final int currentPartCount = parts.size() - 1;
        Log.v(TAG, "part count " + currentPartCount);
        final int lastMessagePartId = parts.get(0).getLastMessagePartId();
        Log.v(TAG, "last message part id " + lastMessagePartId);
        if (currentPartCount < lastMessagePartId) {
            return false;
        }

        boolean[] exists = new boolean[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            int partId = parts.get(i).getMessagePartId();
            if (partId < exists.length) {
                exists[partId] = true;
            }
        }

        for (int i = 0; i < exists.length; i++) {
            if (!exists[i]) {
                return false;
            }
        }

        return true;
    }

    public static String getComplete(Context context, SmsMessage smsMessage) {
        List<IslndMessagePart> parts = getMessageParts(context, smsMessage);
        String[] sections = new String[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            int partId = parts.get(i).getMessagePartId();
            sections[partId] = parts.get(i).getBody();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sections.length; i++) {
            sb.append(sections[i]);
        }

        return sb.toString();
    }

    public static List<IslndMessagePart> getMessageParts(Context context, SmsMessage smsMessage) {
        IslndMessagePart messagePart = buildMessagePart(smsMessage);

        String[] projection = new String[] {
                IslndContract.SmsMessageEntry.COLUMN_ORIGINATING_ADDRESS,
                IslndContract.SmsMessageEntry.COLUMN_MESSAGE_ID,
                IslndContract.SmsMessageEntry.COLUMN_MESSAGE_PART_ID,
                IslndContract.SmsMessageEntry.COLUMN_LAST_MESSAGE_PART_ID,
                IslndContract.SmsMessageEntry.COLUMN_BODY
        };
        String selection = IslndContract.SmsMessageEntry.COLUMN_ORIGINATING_ADDRESS + " = ? AND " +
                IslndContract.SmsMessageEntry.COLUMN_MESSAGE_ID + " = ?";
        String[] selectionArgs = new String[] {
                messagePart.getOriginatingAddress(),
                messagePart.getMessageId()
        };

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.SmsMessageEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);

            List<IslndMessagePart> parts = new ArrayList<>();
            if (!cursor.moveToFirst()) {
                return parts;
            }

            do {
                parts.add(new IslndMessagePart(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getString(4)));
            } while (cursor.moveToNext());

            return parts;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
