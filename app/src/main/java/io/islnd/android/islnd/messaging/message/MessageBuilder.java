package io.islnd.android.islnd.messaging.message;

import android.content.Context;
import android.util.Log;

import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Encoder;
import io.islnd.android.islnd.messaging.PublicIdentity;
import io.islnd.android.islnd.messaging.SecretIdentity;

public class MessageBuilder {
    private static final String TAG = MessageBuilder.class.getSimpleName();

    public static Message buildPublicIdentityMessage(Context context, String mailbox, String nonce, PublicIdentity publicIdentity) {
        return new Message(
                mailbox,
                nonce,
                getNewMessageIdAndUpdate(context),
                MessageType.PUBLIC_IDENTITY,
                new Encoder().encodeToString(publicIdentity.toByteArray()));
    }

    public static Message buildSecretIdentityMessage(Context context, String mailbox, SecretIdentity secretIdentity) {
        return new Message(
                mailbox,
                getNewMessageIdAndUpdate(context),
                MessageType.SECRET_IDENTITY,
                new Encoder().encodeToString(secretIdentity.toByteArray()));
    }

    public static Message buildProfileMessage(Context context, String mailbox, String profileResourceKey) {
        ProfileMessage profileMessage = new ProfileMessage(profileResourceKey);
        return new Message(
                mailbox,
                getNewMessageIdAndUpdate(context),
                MessageType.PROFILE,
                new Encoder().encodeToString(profileMessage.toByteArray()));
    }

    public static Message buildNewAliasMessage(Context context, String mailbox, String newAlias) {
        Log.v(TAG, String.format("message to %s new alias %s", mailbox, newAlias));
        return new Message(
                mailbox,
                getNewMessageIdAndUpdate(context),
                MessageType.NEW_ALIAS,
                newAlias);
    }

    public static Message buildNewGroupKeyMessage(Context context, String mailbox, String encodedKey) {
        Log.v(TAG, String.format("message to %s new group key", mailbox));
        return new Message(
                mailbox,
                getNewMessageIdAndUpdate(context),
                MessageType.NEW_GROUP_KEY,
                encodedKey);
    }

    public static Message buildNewMailboxMessage(Context context, String mailbox, String newMailbox) {
        Log.v(TAG, String.format("message to %s change mailbox to %s", mailbox, newMailbox));
        return new Message(
                mailbox,
                getNewMessageIdAndUpdate(context),
                MessageType.NEW_MAILBOX,
                newMailbox);
    }

    public static Message buildDeleteMeMessage(Context context, String mailbox) {
        Log.v(TAG, String.format("message to %s delete me", mailbox));
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
