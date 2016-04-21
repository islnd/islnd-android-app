package io.islnd.android.islnd.messaging.message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import javax.crypto.SecretKey;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.MailboxHelper;
import io.islnd.android.islnd.messaging.PublicKeyAndInbox;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.crypto.EncryptedMessage;

public class MessagePublisher {
    private static final String TAG = MessagePublisher.class.getSimpleName();

    public static void removeFriend(Context context, int userId) {
        String newAlias = CryptoUtil.createAlias();
        SecretKey newGroupKey = CryptoUtil.getKey();
        createMessagesForRemainingFriends(context, userId, newAlias, newGroupKey);
        DataUtils.updateAlias(context, IslndContract.UserEntry.MY_USER_ID, newAlias);
        DataUtils.updateGroupKey(context, IslndContract.UserEntry.MY_USER_ID, CryptoUtil.encodeKey(newGroupKey));
        Util.setGroupKey(context, newGroupKey);

        createMessageForFriendToRemove(context, userId);
        context.getContentResolver().requestSync(
                Util.getSyncAccount(context),
                IslndContract.CONTENT_AUTHORITY,
                new Bundle());
    }

    private static void createMessageForFriendToRemove(Context context, int userId) {
        EncryptedMessage encryptedMessage = buildEncryptedMessageToRemoveFriend(context, userId);
        ContentValues values = new ContentValues();
        values.put(IslndContract.OutgoingMessageEntry.COLUMN_MAILBOX, encryptedMessage.getMailbox());
        values.put(IslndContract.OutgoingMessageEntry.COLUMN_BLOB, encryptedMessage.getBlob());
        context.getContentResolver().insert(
                IslndContract.OutgoingMessageEntry.CONTENT_URI,
                values
        );
        Log.v(TAG, "inserted 'delete me' message");
    }

    @NonNull
    private static EncryptedMessage buildEncryptedMessageToRemoveFriend(Context context, int userId) {
        String friendToRemoveMailbox = DataUtils.getMessageInbox(context, userId);
        Key friendToRemovePublicKey = DataUtils.getPublicKey(context, userId);
        Message deleteMeMessage = MessageBuilder.buildDeleteMeMessage(
                context,
                friendToRemoveMailbox);
        return new EncryptedMessage(deleteMeMessage, friendToRemovePublicKey, Util.getPrivateKey(context));
    }

    private static void createMessagesForRemainingFriends(Context context, int userIdToRemove, String newAlias, SecretKey newGroupKey) {
        ContentValues[] values = createContentValuesForRemainingFriends(context, userIdToRemove, newAlias, newGroupKey);
        int recordsInserted = context.getContentResolver().bulkInsert(
                IslndContract.OutgoingMessageEntry.CONTENT_URI,
                values
        );
        Log.v(TAG, String.format("wrote %d messages", recordsInserted));
    }

    private static ContentValues[] createContentValuesForRemainingFriends(
            Context context,
            int userIdToRemove,
            String newAlias,
            SecretKey newGroupKey) {

        List<PublicKeyAndInbox> publicKeyAndInboxList = DataUtils.getKeysForOtherUsers(context, userIdToRemove);
        ContentValues[] values = new ContentValues[publicKeyAndInboxList.size() * 3];
        initializeArray(values);

        PrivateKey myPrivateKey = Util.getPrivateKey(context);
        final String newGroupKeyEncoded = CryptoUtil.encodeKey(newGroupKey);

        int index = 0;
        for (PublicKeyAndInbox publicKeyAndInbox : publicKeyAndInboxList) {
            final Message newAliasMessage = MessageBuilder.buildNewAliasMessage(
                    context,
                    publicKeyAndInbox.getInbox(),
                    newAlias
            );
            encryptMessageAndSetToContentValue(
                    newAliasMessage,
                    myPrivateKey,
                    publicKeyAndInbox.getPublicKey(),
                    values[index++],
                    publicKeyAndInbox.getInbox());

            final Message newGroupKeyMessage = MessageBuilder.buildNewGroupKeyMessage(
                    context,
                    publicKeyAndInbox.getInbox(),
                    newGroupKeyEncoded
            );
            encryptMessageAndSetToContentValue(
                    newGroupKeyMessage,
                    myPrivateKey,
                    publicKeyAndInbox.getPublicKey(),
                    values[index++],
                    publicKeyAndInbox.getInbox());

            String newMailbox = MailboxHelper.getAndSetNewInboxForUser(context, publicKeyAndInbox.getPublicKey());
            final Message newMailboxMessage = MessageBuilder.buildNewMailboxMessage(
                    context,
                    publicKeyAndInbox.getInbox(),
                    newMailbox
            );
            encryptMessageAndSetToContentValue(
                    newMailboxMessage,
                    myPrivateKey,
                    publicKeyAndInbox.getPublicKey(),
                    values[index++],
                    publicKeyAndInbox.getInbox());
        }

        return values;
    }

    private static void initializeArray(ContentValues[] values) {
        for (int i = 0; i < values.length; i++) {
            values[i] = new ContentValues();
        }
    }

    private static void encryptMessageAndSetToContentValue(
            Message message,
            PrivateKey myPrivateKey,
            PublicKey publicKey,
            ContentValues value,
            String recipientInbox) {
        EncryptedMessage encryptedAliasMessage = new EncryptedMessage(
                message,
                publicKey,
                myPrivateKey
        );
        value.put(
                IslndContract.OutgoingMessageEntry.COLUMN_MAILBOX,
                recipientInbox);
        value.put(
                IslndContract.OutgoingMessageEntry.COLUMN_BLOB,
                encryptedAliasMessage.getBlob());
    }
}
