package io.islnd.android.islnd.messaging.message;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.security.PublicKey;
import java.util.List;

import javax.crypto.SecretKey;

import io.islnd.android.islnd.app.FriendAddBackService;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.util.ImageUtil;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.InvalidBlobException;
import io.islnd.android.islnd.messaging.MessageLayer;
import io.islnd.android.islnd.messaging.PublicIdentity;
import io.islnd.android.islnd.messaging.ProfileResource;
import io.islnd.android.islnd.messaging.Rest;
import io.islnd.android.islnd.messaging.SecretIdentity;
import io.islnd.android.islnd.messaging.crypto.EncryptedResource;
import io.islnd.android.islnd.messaging.crypto.InvalidSignatureException;
import io.islnd.android.islnd.messaging.server.ResourceQuery;

public class MessageProcessor {

    private static final String TAG = MessageProcessor.class.getSimpleName();

    public static void process(Context context, Message message) {
        if (alreadyProcessed(context, message)) {
            Log.d(TAG, "already processed message with type " + message.getType());
            return;
        }

        int type = message.getType();
        switch (type) {
            case MessageType.PUBLIC_IDENTITY: {
                Log.d(TAG, "process public identity");

                if (!DataUtils.validateMessage(context, message)) {
                    Log.w(TAG, "public identity message was not valid");
                    return;
                }

                PublicIdentity friendPublicIdentity = null;
                try {
                    friendPublicIdentity = PublicIdentity.fromProto(message.getBlob());
                } catch (InvalidProtocolBufferException e) {
                    Log.w(TAG, e.toString());
                    e.printStackTrace();
                    break;
                }

                DataUtils.addOrUpdateUser(
                        context,
                        friendPublicIdentity.getPublicKey(),
                        friendPublicIdentity.getMessageInbox(),
                        message.getMailbox());

                startAddBackJob(context, friendPublicIdentity, FriendAddBackService.SECRET_IDENTITY_JOB);
                startAddBackJob(context, friendPublicIdentity, FriendAddBackService.PROFILE_JOB);
                startAddBackJob(context, friendPublicIdentity, FriendAddBackService.NEW_MAILBOX_JOB);
                break;
            }
            case MessageType.SECRET_IDENTITY: {
                Log.d(TAG, "process secret identity");

                SecretIdentity friendSecretIdentity = SecretIdentity.fromProto(message.getBlob());
                addSecretIdentity(context, friendSecretIdentity, message.getMailbox());
                break;
            }
            case MessageType.PROFILE: {
                Log.d(TAG, "process profile");
                processProfileMessage(context, message);
                break;
            }
            case MessageType.NEW_ALIAS: {
                Log.d(TAG, "process new alias");
                int userID = DataUtils.getUserIdForMessageOutbox(context, message.getMailbox());
                DataUtils.updateAlias(context, userID, message.getBlob());
                break;
            }
            case MessageType.NEW_GROUP_KEY: {
                Log.v(TAG, "process new group key");
                int userID = DataUtils.getUserIdForMessageOutbox(context, message.getMailbox());
                DataUtils.updateGroupKey(context, userID, message.getBlob());
                break;
            }
            case MessageType.NEW_MAILBOX: {
                Log.d(TAG, "process new mailbox");
                int userID = DataUtils.getUserIdForMessageOutbox(context, message.getMailbox());
                Log.v(TAG, String.format("user %d from %s to %s",
                        userID, message.getMailbox(), message.getBlob()));
                DataUtils.removeMessageToken(context, message.getMailbox());
                DataUtils.updateUserOutbox(context, userID, message.getBlob());
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
                ProfileResource profileResource = encryptedResources.get(0).decryptAndVerify(
                        groupKey,
                        publicKey
                );

                saveProfile(context, message, profileResource);
            } catch (InvalidSignatureException e) {
                Log.w(TAG, e.toString() + "could not decrypt and verify event!");
                Log.w(TAG, e.toString());
            } catch (InvalidBlobException e) {
                Log.w(TAG, "blob was invalid!");
                Log.w(TAG, e.toString());
            } catch (InvalidProtocolBufferException e) {
                Log.w(TAG, "protocol buffer was invalid!");
                Log.w(TAG, e.toString());
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

    private static void addSecretIdentity(Context context, SecretIdentity friendSecretIdentity, String friendOutbox) {
        int userId = DataUtils.getUserIdForMessageOutbox(context, friendOutbox);

        MessageLayer.addSecretIdentityAndCreateDefaultProfile(
                context,
                userId,
                friendSecretIdentity);
    }

    private static void startAddBackJob(Context context, PublicIdentity publicIdentity, int profileJob) {
        Intent sendProfileIntent = new Intent(context, FriendAddBackService.class);
        sendProfileIntent.putExtra(
                FriendAddBackService.MAILBOX_EXTRA,
                publicIdentity.getMessageInbox());
        sendProfileIntent.putExtra(
                FriendAddBackService.JOB_EXTRA,
                profileJob);
        context.startService(sendProfileIntent);
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
