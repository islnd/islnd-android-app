package org.island.messaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.island.island.Database.FriendDatabase;
import com.island.island.Models.Comment;
import com.island.island.Models.Post;
import com.island.island.Models.User;
import com.island.island.R;
import com.island.island.Utils.Utils;

import org.island.messaging.proto.IslandProto;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class MessageLayer {
    private static final String TAG = MessageLayer.class.getSimpleName();

    public static List<User> getReaders(Context context, String username, Key privateKey) {
        //call the REST service
        //FriendDatabase.getInstance(context).deleteAll();
        List<EncryptedPseudonymKey> keys = Rest.getReaders(username);
        if (keys == null) {
            Log.d(TAG, "get readers returned null");
            return new ArrayList<>();
        }

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
        FriendDatabase friendDatabase = FriendDatabase.getInstance(context);

        ArrayList<PseudonymKey> keys = friendDatabase.getKeys();
        for (PseudonymKey pk : keys) {
            Log.v(TAG, "pk: " + pk.getUsername());
        }

        List<Post> posts = new ArrayList<>();

        for (PseudonymKey key: keys) {
            List<EncryptedPost> encryptedPosts = Rest.getPosts(key.getPseudonym());
            if (encryptedPosts == null) {
                Log.d(TAG, "get posts return null");
                continue;
            }

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

    public static void post(Context context, String content) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastId = preferences.getString(context.getString(R.string.post_id_key), "");
        String newId = String.valueOf(Integer.parseInt(lastId) + 1);
        PostUpdate postUpdate = PostUpdate.buildPost(content, newId);

        String myGroupKey = preferences.getString(context.getString(R.string.group_key), "");
        Key key = Crypto.decodeSymmetricKey(myGroupKey);
        String encryptedPost = ObjectEncrypter.encryptSymmetric(postUpdate, key);

        String myPseudonym = preferences.getString(context.getString(R.string.pseudonym_key), "");
        Rest.post(myPseudonym, encryptedPost);
    }

    public static String getPseudonym(String seed) {
        return Rest.getPseudonym(seed);
    }

    public static void addFriendFromQRCode(Context context, String qrCode) {
        Log.v(TAG, "adding friend from QR code: " + qrCode);
        byte[] bytes = new Decoder().decode(qrCode);
        PseudonymKey pk = PseudonymKey.fromProto(bytes);
        FriendDatabase friendDatabase = FriendDatabase.getInstance(context);
        if (friendDatabase.contains(pk)) {
            Toast.makeText(
                    context,
                    String.format("%s is already your friend!", pk.getUsername()),
                    Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(
                    context,
                    String.format("%s is now your friend!", pk.getUsername()),
                    Toast.LENGTH_LONG).show();
            friendDatabase.addFriend(pk);
        }
    }

    public static String getQrCode(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long uniqueId = sharedPreferences.getLong(context.getString(R.string.pseudonym_key_id), 0);
        String username = sharedPreferences.getString(context.getString(R.string.user_name), "");
        String pseudonym = sharedPreferences.getString(context.getString(R.string.pseudonym), "");
        Key groupKey = Crypto.decodeSymmetricKey(
                sharedPreferences.getString(context.getString(R.string.group_key), ""));

        PseudonymKey pk = new PseudonymKey(uniqueId, username, pseudonym, groupKey);
        String qrCode = new Encoder().encodeToString(pk.toByteArray());
        Log.v(TAG, "generated QR code: " + qrCode);
        return qrCode;
    }
}
