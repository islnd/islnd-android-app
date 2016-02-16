package org.island.messaging;

import android.content.Context;
import android.util.Log;

import com.island.island.Database.FriendDatabase;
import com.island.island.Models.Comment;
import com.island.island.Models.Post;
import com.island.island.Models.User;
import com.island.island.Utils.Utils;

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
        //FriendDatabase.getInstance(context).deleteAll();
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

    public static List<Post> getPosts(Context context) {
        ArrayList<PseudonymKey> keys = FriendDatabase.getInstance(context).getKeys();
        for (PseudonymKey pk : keys) {
            Log.v(TAG, "pk: " + pk.getUsername());
        }

        List<Post> posts = new ArrayList<>();

        for (PseudonymKey key: keys) {
            List<EncryptedPost> encryptedPosts = Rest.getPosts(key.getPseudonym());

            for (EncryptedPost post: encryptedPosts) {
                SignedObject signedPost = SignedObject.
                        fromProto(ObjectEncrypter.decryptSymmetric(post.blob, key.getKey()));
                //--TODO check that post is signed
                PostUpdate postUpdate = PostUpdate.fromProto(signedPost.getObject());

                if (postUpdate != null
                        && !postUpdate.isDeletion()) {
                    posts.add(new Post(key.getUsername(),
                            Utils.smartTimestampFromUnixTime(postUpdate.getTimestamp()),
                            postUpdate.getContent(),
                            new ArrayList<Comment>()));
                    Log.v(TAG, "timestamp: " + postUpdate.getTimestamp());
                }
            }
        }

        return posts;
    }
}
