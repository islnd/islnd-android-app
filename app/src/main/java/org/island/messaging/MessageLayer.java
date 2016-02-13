package org.island.messaging;

import com.island.island.Models.User;
import com.island.island.Utils.Utils;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by poo on 2/13/16.
 */
public class MessageLayer {
    public static List<User> getReaders(String username) {
        //call the REST service
        List<EncryptedPseudonymKey> keys = Rest.getReaders(username);

        //decrypt the friends
        List<User> friends = new ArrayList<>();
        for (EncryptedPseudonymKey encryptedPseudonymKey : keys) {
            PseudonymKey pseudonymKey = (PseudonymKey) ObjectEncrypter.decryptAsymmetric(encryptedPseudonymKey.blob, null);
            friends.add(new User(pseudonymKey.getUsername()));
        }

        return friends;
    }

    public static void postPublicKey(String username, Key publicKey){
        Rest.postPublicKey(username, Utils.convertToString(publicKey));
    }
}
