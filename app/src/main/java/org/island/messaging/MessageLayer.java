package org.island.messaging;

import android.content.Context;
import android.util.Log;

import com.island.island.Database.FriendDatabase;
import com.island.island.Models.User;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by poo on 2/13/16.
 */
public class MessageLayer {
    private static final String TAG = MessageLayer.class.getSimpleName();

    public static List<User> getReaders(Context context, String username, Key privateKey) {
        //call the REST service
        List<EncryptedPseudonymKey> keys = Rest.getReaders(username);

        //decrypt the friends and add to DB
        List<User> friends = new ArrayList<>();
        for (EncryptedPseudonymKey encryptedPseudonymKey : keys) {
            PseudonymKey pseudonymKey = ObjectEncrypter.decryptPseudonymKey(
                    encryptedPseudonymKey.blob,
                    privateKey);

            FriendDatabase friendDatabase = FriendDatabase.getInstance(context);
            if (!friendDatabase.contains(pseudonymKey)) {
                friendDatabase.addFriend(pseudonymKey);
            }

            friends.add(new User(pseudonymKey.getUsername()));
        }

        return friends;
    }

    public static void postPublicKey(String username, Key publicKey){
        Rest.postPublicKey(username, Crypto.encodeKey(publicKey));
    }
}
