package io.islnd.android.islnd.messaging;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.security.Key;

import io.islnd.android.islnd.app.FindNewFriendService;
import io.islnd.android.islnd.app.FriendAddBackService;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.util.Util;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

public class MessageLayer {
    private static final String TAG = MessageLayer.class.getSimpleName();

    public static boolean addFriendFromEncodedIdentityString(Context context,
                                                             String encodedString) {
        context.stopService(new Intent(context, FindNewFriendService.class));

        Identity identity = Identity.fromProto(encodedString);
        boolean newFriend =  addFriendToDatabaseAndCreateDefaultProfile(context, identity);
        if (!newFriend) {
            return false;
        }

        Intent findFriendServiceIntent = new Intent(context, FindNewFriendService.class);
        context.startService(findFriendServiceIntent);

        Intent addBackFriendIntent = new Intent(context, FriendAddBackService.class);
        addBackFriendIntent.putExtra(FriendAddBackService.MAILBOX_EXTRA, identity.getMessageInbox());
        context.startService(addBackFriendIntent);

        return newFriend;
    }

    public static boolean addFriendToDatabaseAndCreateDefaultProfile(Context context, Identity identity) {
        if (DataUtils.containsPublicKey(context, identity.getPublicKey())) {
            return false;
        }

        long userId = DataUtils.insertUser(context, identity);

        Profile profile = Util.buildDefaultProfile(context, identity.getDisplayName());
        DataUtils.insertProfile(context, profile, userId);

        return true;
    }

    public static Identity getMyIdentity(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //--TODO get display name without a cursor
        String displayName = DataUtils.getDisplayName(context, Util.getUserId(context));
        String alias = DataUtils.getMostRecentAlias(context, Util.getUserId(context));
        String messageInbox = DataUtils.getMessageInbox(context, Util.getUserId(context));
        Log.v(TAG, String.format("alias is %s", alias));
        Key groupKey = CryptoUtil.decodeSymmetricKey(
                sharedPreferences.getString(context.getString(R.string.group_key), ""));
        Key publicKey = CryptoUtil.decodePublicKey(
                sharedPreferences.getString(context.getString(R.string.public_key), ""));

        return new Identity(displayName, alias, messageInbox, groupKey, publicKey);
    }

    public static long getServerTimeOffsetMillis(Context context, int repetitions) throws IOException {
        return Rest.getServerTimeOffsetMillis(repetitions, Util.getApiKey(context));
    }
}
