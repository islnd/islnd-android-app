package io.islnd.android.islnd.messaging;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.security.Key;
import java.security.PublicKey;

import io.islnd.android.islnd.app.FriendAddBackService;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.RepeatSyncService;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

public class MessageLayer {
    private static final String TAG = MessageLayer.class.getSimpleName();

    public static boolean addPublicIdentityFromSms(Context context, String encodedString) {
        Log.d(TAG, "addPublicIdentityFromSms");
        Log.v(TAG, "encodedString: " + encodedString);

        PublicIdentity friendPublicIdentity = null;
        try {
            friendPublicIdentity = PublicIdentity.fromProto(encodedString);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.toString());
            return false;
        } catch (InvalidProtocolBufferException e) {
            Log.w(TAG, e.toString());
            return false;
        }

        if (DataUtils.getPublicKey(context, IslndContract.UserEntry.MY_USER_ID)
                .equals(friendPublicIdentity.getPublicKey())) {
            Toast.makeText(context, context.getText(R.string.sms_add_self_message), Toast.LENGTH_LONG).show();
            Log.d(TAG, "can't sms ourselves");
            return false;
        }

        return addFriendAndStartAddBackJobs(context, friendPublicIdentity);
    }

    public static boolean addPublicIdentityFromQrCode(Context context, String encodedString) {
        Log.d(TAG, "addPublicIdentityFromQrCode");
        PublicIdentity friendPublicIdentity = null;
        try {
            friendPublicIdentity = PublicIdentity.fromProto(encodedString);
        } catch (InvalidProtocolBufferException e) {
            Log.w(TAG, e.toString());
            return false;
        }

        return addFriendAndStartAddBackJobs(context, friendPublicIdentity);
    }

    private static boolean addFriendAndStartAddBackJobs(Context context, PublicIdentity friendPublicIdentity) {
        boolean newFriend = DataUtils.addOrUpdateUser(
                context,
                friendPublicIdentity.getPublicKey(),
                friendPublicIdentity.getMessageInbox());

        Log.v(TAG, "friend wants inbox " + friendPublicIdentity.getMessageInbox());

        //--Check for friend's profile
        Intent repeatSyncServiceIntent = new Intent(context, RepeatSyncService.class);
        context.startService(repeatSyncServiceIntent);

        //--Send our identity and profile to friend
        final String friendInbox = friendPublicIdentity.getMessageInbox();
        final String randomValue = friendPublicIdentity.getNonce();
        startAddBackJob(context, friendInbox, randomValue, FriendAddBackService.PUBLIC_IDENTITY_JOB);
        startAddBackJob(context, friendInbox, randomValue, FriendAddBackService.SECRET_IDENTITY_JOB);
        startAddBackJob(context, friendInbox, randomValue, FriendAddBackService.PROFILE_JOB);

        return newFriend;
    }

    private static void startAddBackJob(Context context, String destinationMailbox, String nonce, int identityJob) {
        Intent sendIdentityIntent = new Intent(context, FriendAddBackService.class);
        sendIdentityIntent.putExtra(FriendAddBackService.MAILBOX_EXTRA, destinationMailbox);
        sendIdentityIntent.putExtra(FriendAddBackService.NONCE_EXTRA, nonce);
        sendIdentityIntent.putExtra(FriendAddBackService.JOB_EXTRA, identityJob);
        context.startService(sendIdentityIntent);
    }

    public static boolean addSecretIdentityAndCreateDefaultProfile(
            Context context,
            int userId,
            SecretIdentity secretIdentity) {
        DataUtils.addOrUpdateSecretIdentity(context, userId, secretIdentity);
        Profile profile = Util.buildDefaultProfile(context, secretIdentity.getDisplayName());
        DataUtils.insertProfile(context, profile, userId);

        //--TODO how do we handle people already our friends?
        DataUtils.insertNewFriendNotification(context, userId);
        return true;
    }

    public static PublicIdentity createNewPublicIdentity(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String messageInbox = MailboxHelper.getAndSetMyNewInbox(context);

        PublicKey publicKey = CryptoUtil.decodePublicKey(
                sharedPreferences.getString(context.getString(R.string.public_key), ""));
        final String nonce = CryptoUtil.getNewNonce();

        DataUtils.addMessageToken(context, messageInbox, nonce);

        return new PublicIdentity(messageInbox, publicKey, nonce);
    }

    public static SecretIdentity getMySecretIdentity(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String alias = DataUtils.getMostRecentAlias(context, IslndContract.UserEntry.MY_USER_ID);
        String displayName = DataUtils.getDisplayName(context, IslndContract.UserEntry.MY_USER_ID);
        Key groupKey = CryptoUtil.decodeSymmetricKey(
                sharedPreferences.getString(context.getString(R.string.group_key), ""));

        return new SecretIdentity(displayName, alias, groupKey);
    }

    public static long getServerTimeOffsetMillis(Context context, int repetitions) throws IOException {
        return Rest.getServerTimeOffsetMillis(repetitions, Util.getApiKey(context));
    }
}
