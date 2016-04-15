package io.islnd.android.islnd.messaging.message;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.security.PublicKey;
import java.util.List;

import javax.crypto.SecretKey;

import io.islnd.android.islnd.app.FriendAddBackService;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.Identity;
import io.islnd.android.islnd.messaging.MessageLayer;
import io.islnd.android.islnd.messaging.ProfileResource;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.crypto.EncryptedResource;
import io.islnd.android.islnd.messaging.crypto.InvalidSignatureException;
import io.islnd.android.islnd.messaging.server.ResourceQuery;

public class MessageProcessor {

    private static final String TAG = MessageProcessor.class.getSimpleName();

    public static void process(Context context, Message message) {
        if (alreadyProcessed(context, message)) {
            return;
        }

        int type = message.getType();
        switch (type) {
            case MessageType.IDENTITY: {
                Log.v(TAG, "process identity");

                Identity friendToAdd = Identity.fromProto(message.getBlob());
                addNewFriend(context, friendToAdd);

                sendOurProfileToNewFriend(context, friendToAdd);
                break;
            }
            case MessageType.PROFILE: {
                Log.v(TAG, "process profile");
                processProfileMessage(context, message);
                break;
            }
            case MessageType.NEW_ALIAS: {
                Log.v(TAG, "process new alias");
                int userID = DataUtils.getUserIdForMessageOutbox(context, message.getMailbox());
                DataUtils.updateAlias(context, userID, message.getBlob());
                break;
            }
            case MessageType.DELETE_ME: {
                Log.v(TAG, "process delete user");
                int userID = DataUtils.getUserIdForMessageOutbox(context, message.getMailbox());
                if (userID > 0) {
                    DataUtils.markUserAsDeletedAndDeletePosts(context, userID);
                }
                else {
                    Log.d(TAG, "user already deleted or their outbox was modified");
                }

                break;
            }
        }

        recordProcessed(context, message);
    }

    private static void processProfileMessage(Context context, Message message) {
        ProfileMessage profileMessage = ProfileMessage.fromProto(message.getBlob());
        ResourceQuery resourceQuery = new ResourceQuery(profileMessage.getResourceKey());
        List<EncryptedResource> encryptedResources = Rest.postResourceQuery(
                resourceQuery,
                Util.getApiKey(context)
        );

        if (encryptedResources != null
                && encryptedResources.size() > 0) {
            int userId = DataUtils.getUserIdForMessageOutbox(context, message.getMailbox());
            SecretKey groupKey = DataUtils.getGroupKey(context, userId);
            PublicKey publicKey = DataUtils.getPublicKey(context, userId);
            try {
                ProfileResource profileResource = (ProfileResource) encryptedResources.get(0).decryptAndVerify(
                        groupKey,
                        publicKey
                );

                saveProfile(context, message, profileResource);
                updateMailbox(context);
            } catch (InvalidSignatureException e) {
                e.printStackTrace();
            }
        }
    }

    private static void recordProcessed(Context context, Message message) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.ReceivedMessageEntry.COLUMN_MAILBOX, message.getMailbox());
        values.put(IslndContract.ReceivedMessageEntry.COLUMN_MESSAGE_ID, message.getMessageId());
        context.getContentResolver().insert(
                IslndContract.ReceivedMessageEntry.CONTENT_URI,
                values
        );
    }

    private static boolean alreadyProcessed(Context context, Message message) {
        String[] projection = new String[] {
                IslndContract.ReceivedMessageEntry._ID
        };

        String selection = IslndContract.ReceivedMessageEntry.COLUMN_MAILBOX + " = ? AND " +
                IslndContract.ReceivedMessageEntry.COLUMN_MESSAGE_ID + " = ?";
        String[] args = new String[] {
            message.getMailbox(),
            Integer.toString(message.getMessageId())
        };

        Cursor cursor = null;
        boolean alreadyProcessed;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.ReceivedMessageEntry.CONTENT_URI,
                    projection,
                    selection,
                    args,
                    null
            );

            alreadyProcessed = cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return alreadyProcessed;
    }

    private static void addNewFriend(Context context, Identity friendToAdd) {
        Identity friendWithOurMailbox = new Identity(
                friendToAdd.getDisplayName(),
                friendToAdd.getAlias(),
                friendToAdd.getMessageInbox(),
                friendToAdd.getGroupKey(),
                friendToAdd.getPublicKey()
        );

        MessageLayer.addFriendToDatabaseAndCreateDefaultProfile(
                context,
                friendWithOurMailbox,
                Util.getMyInbox(context)
        );

        //--We need a new inbox to give to our next friend
        Util.setMyInbox(context, CryptoUtil.createAlias());
    }

    private static void sendOurProfileToNewFriend(Context context, Identity friendToAdd) {
        Intent sendProfileIntent = new Intent(context, FriendAddBackService.class);
        sendProfileIntent.putExtra(
                FriendAddBackService.MAILBOX_EXTRA,
                friendToAdd.getMessageInbox());
        sendProfileIntent.putExtra(
                FriendAddBackService.JOB_EXTRA,
                FriendAddBackService.PROFILE_JOB);
        context.startService(sendProfileIntent);
    }

    private static void updateMailbox(Context context) {
        //--We need a new inbox for the next friend we make
        final String newMailbox = CryptoUtil.createAlias();
        DataUtils.updateMyUserMailbox(context, newMailbox);
        Util.setMyInbox(context, newMailbox);

        Log.v(TAG, "my new mailbox is " + newMailbox);
    }

    private static void saveProfile(Context context, Message message, ProfileResource profileResource) {
        Uri headerImageUri = ImageUtil.saveBitmapToInternalFromByteArray(
                context,
                profileResource.getHeaderImageBytes());
        Uri profileImageUri = ImageUtil.saveBitmapToInternalFromByteArray(
                context,
                profileResource.getProfileImageBytes());
        Profile profile = new Profile(
                profileResource.getAboutMe(),
                profileImageUri,
                headerImageUri
        );

        int userId = DataUtils.getUserIdForMessageOutbox(context, message.getMailbox());
        Log.v(TAG, "adding profile for user " + userId);
        DataUtils.insertProfile(context, profile, userId);
    }
}
