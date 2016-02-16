package org.island.messaging;

import com.island.island.Models.User;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by poo on 2/13/16.
 */
public class MessageLayer {
    public static List<User> getReaders(String username, Key privateKey) {
        //call the REST service
        List<EncryptedPseudonymKey> keys = Rest.getReaders(username);

        //decrypt the friends
        List<User> friends = new ArrayList<>();
        for (EncryptedPseudonymKey encryptedPseudonymKey : keys) {
            PseudonymKey pseudonymKey = ObjectEncrypter.decryptPseudonymKey(encryptedPseudonymKey.blob, privateKey);
            friends.add(new User(pseudonymKey.getUsername()));
        }

        return friends;
    }

    public static void postPublicKey(String username, Key publicKey){
        Rest.postPublicKey(username, Crypto.encodeKey(publicKey));
    }
}
