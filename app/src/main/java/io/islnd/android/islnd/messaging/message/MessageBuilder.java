package io.islnd.android.islnd.messaging.message;

import android.content.Context;
import android.util.Log;

import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Encoder;
import io.islnd.android.islnd.messaging.Identity;

public class MessageBuilder {
    private static final String TAG = MessageBuilder.class.getSimpleName();

    public static Message buildIdentityMessage(Context context, String mailbox, Identity identity) {
        return new Message(
                mailbox,
                getNewMessageIdAndUpdate(context),
                MessageType.IDENTITY,
                new Encoder().encodeToString(identity.toByteArray()));
    }

    public static Message buildProfileMessage(Context context, String mailbox, ProfileMessage profileMessage) {
        return new Message(
                mailbox,
                getNewMessageIdAndUpdate(context),
                MessageType.PROFILE,
                new Encoder().encodeToString(profileMessage.toByteArray()));
    }

    public static Message buildNewAliasMessage(Context context, String mailbox, String newAlias) {
        return new Message(
                mailbox,
                getNewMessageIdAndUpdate(context),
                MessageType.NEW_ALIAS,
                newAlias);
    }

    public static Message buildNewGroupKeyMessage(Context context, String mailbox, String encodedKey) {
        return new Message(
                mailbox,
                getNewMessageIdAndUpdate(context),
                MessageType.NEW_GROUP_KEY,
                encodedKey);
    }

    public static Message buildDeleteMeMessage(Context context, String mailbox) {
        return new Message(
                mailbox,
                getNewMessageIdAndUpdate(context),
                MessageType.DELETE_ME,
                "");
    }

    private static int getNewMessageIdAndUpdate(Context context) {
        int currentMessageId = Util.getMessageId(context);
        int newMessageId = currentMessageId + 1;
        Log.v(TAG, "save message id " + newMessageId);
        Util.setMessageId(context, newMessageId);
        return newMessageId;
    }
}
