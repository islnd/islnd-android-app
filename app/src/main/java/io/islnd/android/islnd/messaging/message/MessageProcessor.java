package io.islnd.android.islnd.messaging.message;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.security.Key;
import java.util.List;

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
import io.islnd.android.islnd.messaging.proto.IslandProto;
import io.islnd.android.islnd.messaging.server.ResourceQuery;

public class MessageProcessor {

    private static final String TAG = MessageProcessor.class.getSimpleName();

    public static boolean process(Context context, Message message) {
        Log.v(TAG, "message type " + message.getType());
        if (message.getType() == MessageType.IDENTITY) {
            Log.v(TAG, "process identity");

            Identity friendToAdd = Identity.fromProto(message.getBlob());
            addNewFriend(context, friendToAdd);

            sendOurProfileToNewFriend(context, friendToAdd);

            return false;
        }
        else if (message.getType() == MessageType.PROFILE) {
            Log.v(TAG, "process profile");

            ProfileMessage profileMessage = ProfileMessage.fromProto(message.getBlob());
            ResourceQuery resourceQuery = new ResourceQuery(profileMessage.getResourceKey());
            List<EncryptedResource> encryptedResources = Rest.postResourceQuery(
                    resourceQuery,
                    Util.getApiKey(context)
            );

            if (encryptedResources != null
                    && encryptedResources.size() > 0) {
                int userId = DataUtils.getUserIdWithMessageOutbox(context, message.getMailbox());
                Key groupKey = DataUtils.getGroupKey(context, userId);
                Key publicKey = DataUtils.getPublicKey(context, userId);
                try {
                    ProfileResource profileResource = (ProfileResource) encryptedResources.get(0).decryptAndVerify(
                            groupKey,
                            publicKey
                    );

                    saveProfile(context, message, profileResource);
                    updateMailbox(context, message);
                } catch (InvalidSignatureException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        return false;
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
    }

    private static void sendOurProfileToNewFriend(Context context, Identity friendToAdd) {
        Intent sendProfileIntent = new Intent(context, FriendAddBackService.class);
        sendProfileIntent.putExtra(FriendAddBackService.MAILBOX_EXTRA, friendToAdd.getMessageInbox());
        sendProfileIntent.putExtra(
                FriendAddBackService.JOB_EXTRA,
                FriendAddBackService.PROFILE_JOB);
        context.startService(sendProfileIntent);
    }

    private static void updateMailbox(Context context, Message message) {
        //--This is a hack to stop querying the mailbox because
        //  it assumes we won't get any more messages
        removeMailboxFromQuerySet(context, message.getMailbox());

        //--Update our inbox for the next friend we make
        //  and add it to the query set
        final String newMailbox = CryptoUtil.createAlias();
        DataUtils.updateMyUserMailbox(context, newMailbox);
        Util.setMyMailbox(context, newMailbox);
        DataUtils.addMailboxToQuerySet(context, newMailbox);

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

        int userId = DataUtils.getUserIdWithMessageOutbox(context, message.getMailbox());
        DataUtils.insertProfile(context, profile, userId);
        Log.v(TAG, "adding profile for user " + userId);
    }

    private static void removeMailboxFromQuerySet(Context context, String mailbox) {
        String selection = IslndContract.MailboxEntry.COLUMN_MAILBOX + " = ?";
        String[] args = new String[] {
                mailbox
        };
        context.getContentResolver().delete(
                IslndContract.MailboxEntry.CONTENT_URI,
                selection,
                args
        );
    }
}
