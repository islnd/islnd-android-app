package io.islnd.android.islnd.messaging;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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

    public static boolean addPublicIdentityFromQrCode(Context context, String encodedString) {
        Log.d(TAG, "addPublicIdentityFromQrCode");
        PublicIdentity friendPublicIdentity = PublicIdentity.fromProto(encodedString);
        boolean newFriend = DataUtils.addOrUpdateUser(
                context,
                friendPublicIdentity,
                Util.getMyInbox(context));  //--My current inbox will be where I check for new
                                            //  messages from this user

        //--We need a new inbox to give to our next friend
        Util.setMyInbox(context, CryptoUtil.createAlias());

        //--Check for friend's profile
        Intent repeatSyncServiceIntent = new Intent(context, RepeatSyncService.class);
        context.startService(repeatSyncServiceIntent);

        //--Send our identity and profile to friend
        final String friendInbox = friendPublicIdentity.getMessageInbox();
        startAddBackJob(context, friendInbox, FriendAddBackService.PUBLIC_IDENTITY_JOB);
        startAddBackJob(context, friendInbox, FriendAddBackService.SECRET_IDENTITY_JOB);
        startAddBackJob(context, friendInbox, FriendAddBackService.PROFILE_JOB);

        return newFriend;
    }

    private static void startAddBackJob(Context context, String friendInbox, int identityJob) {
        Intent sendIdentityIntent = new Intent(context, FriendAddBackService.class);
        sendIdentityIntent.putExtra(FriendAddBackService.MAILBOX_EXTRA, friendInbox);
        sendIdentityIntent.putExtra(
                FriendAddBackService.JOB_EXTRA,
                identityJob);
        context.startService(sendIdentityIntent);
    }

    public static boolean addSecretIdentityAndCreateDefaultProfile(
            Context context,
            int userId,
            SecretIdentity secretIdentity) {
        DataUtils.addSecretIdentity(context, userId, secretIdentity);
        Profile profile = Util.buildDefaultProfile(context, secretIdentity.getDisplayName());
        DataUtils.insertProfile(context, profile, userId);

        //--TODO how do we handle people already our friends?
        DataUtils.insertNewFriendNotification(context, userId);
        return true;
    }

    public static PublicIdentity getMyPublicIdentity(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String messageInbox = DataUtils.getMessageInbox(context, IslndContract.UserEntry.MY_USER_ID);
        PublicKey publicKey = CryptoUtil.decodePublicKey(
                sharedPreferences.getString(context.getString(R.string.public_key), ""));

        return new PublicIdentity(messageInbox, publicKey);
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
