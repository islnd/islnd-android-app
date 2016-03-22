package io.islnd.android.islnd.messaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.models.Profile;
import io.islnd.android.islnd.app.R;
import io.islnd.android.islnd.app.util.Util;

import java.security.Key;

public class MessageLayer {
    private static final String TAG = MessageLayer.class.getSimpleName();

    public static String getPseudonym(Context context, String seed) {
        return Rest.getPseudonym(seed, Util.getApiKey(context));
    }

    public static Boolean getPing(String apiKey) {
        return Rest.getPing(apiKey);
    }

    public static boolean addFriendFromEncodedIdentityString(Context context,
                                                             String encodedString) {
        Log.v(TAG, "adding friend from encoded string: " + encodedString);
        byte[] bytes = new Decoder().decode(encodedString);
        Identity pk = Identity.fromProto(bytes);
        return addFriendToDatabaseAndCreateDefaultProfile(context, pk);
    }

    private static boolean addFriendToDatabaseAndCreateDefaultProfile(Context context, Identity pk) {
        //--TODO only add if not already friends
        long userId = DataUtils.insertUser(context, pk);

        Profile profile = Util.buildDefaultProfile(context, pk.getDisplayName());
        DataUtils.insertProfile(context, profile, userId);

        return true;
    }

    public static String getEncodedIdentityString(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //--TODO get display name without a cursor
        String displayName = DataUtils.getDisplayName(context, Util.getUserId(context));
        String alias = sharedPreferences.getString(context.getString(R.string.alias), "");
        Log.v(TAG, String.format("alias is %s", alias));
        Key groupKey = CryptoUtil.decodeSymmetricKey(
                sharedPreferences.getString(context.getString(R.string.group_key), ""));
        Key publicKey = CryptoUtil.decodePublicKey(
                sharedPreferences.getString(context.getString(R.string.public_key), ""));

        Identity pk = new Identity(displayName, alias, groupKey, publicKey);
        String encodeString = new Encoder().encodeToString(pk.toByteArray());
        Log.v(TAG, "generated encoded string: " + encodeString);
        return encodeString;
    }
}
