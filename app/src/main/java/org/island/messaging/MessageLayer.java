package org.island.messaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.island.island.Database.FriendDatabase;
import com.island.island.Models.Post;
import com.island.island.Models.Profile;
import com.island.island.Models.User;
import com.island.island.R;
import com.island.island.Utils.Utils;

import org.island.messaging.crypto.CryptoUtil;
import org.island.messaging.crypto.EncryptedData;
import org.island.messaging.crypto.EncryptedPost;
import org.island.messaging.crypto.EncryptedProfile;
import org.island.messaging.crypto.ObjectEncrypter;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class MessageLayer {
    private static final String TAG = MessageLayer.class.getSimpleName();

    public static List<User> getReaders(Context context, String username, Key privateKey) {
        //call the REST service
        //FriendDatabase.getInstance(context).deleteAll();
        List<EncryptedData> keys = Rest.getReaders(username);
        if (keys == null) {
            Log.d(TAG, "get readers returned null");
            return new ArrayList<>();
        }

        //decrypt the friends and add to DB
        List<User> friends = new ArrayList<>();
        for (EncryptedData encryptedPseudonymKey : keys) {
            PseudonymKey pseudonymKey = ObjectEncrypter.decryptPseudonymKey(
                    encryptedPseudonymKey.getBlob(),
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
        Rest.postPublicKey(username, CryptoUtil.encodeKey(publicKey));
    }

    public static List<Post> getPosts(Context context) {
        FriendDatabase friendDatabase = FriendDatabase.getInstance(context);

        ArrayList<PseudonymKey> keys = friendDatabase.getKeys();
        List<Post> posts = new ArrayList<>();

        for (PseudonymKey key: keys) {
            List<EncryptedPost> encryptedPosts = Rest.getPosts(key.getPseudonym());
            Log.v(TAG, "posts from " + key.getUsername());
            Log.v(TAG, "posts from " + key.getPseudonym());
            if (encryptedPosts == null) {
                Log.d(TAG, "get posts return null");
                continue;
            }

            Log.v(TAG, encryptedPosts.size() + " posts from " + key.getUsername());
            for (EncryptedPost post: encryptedPosts) {
                //--TODO check that post is signed
                PostUpdate postUpdate = post.decrypt(key.getKey());

                if (postUpdate != null
                        && !postUpdate.isDeletion()) {
                    posts.add(new Post(
                                    key.getUsername(),
                                    Utils.smartTimestampFromUnixTime(postUpdate.getTimestamp()),
                                    postUpdate.getContent(),
                                    new ArrayList<>()));
                }
            }
        }

        return posts;
    }

    public static void post(Context context, String content) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastId = preferences.getString(context.getString(R.string.post_id_key), "0");
        String newId = String.valueOf(Integer.parseInt(lastId) + 1);
        PostUpdate postUpdate = PostUpdate.buildPost(content, newId);
        String privateKey = preferences.getString(context.getString(R.string.private_key), "");
        String myGroupKey = preferences.getString(context.getString(R.string.group_key), "");

        EncryptedPost encryptedPost = new EncryptedPost(
                postUpdate,
                CryptoUtil.decodePrivateKey(privateKey),
                CryptoUtil.decodeSymmetricKey(myGroupKey));

        String pseudonymSeed = preferences.getString(context.getString(R.string.pseudonym_seed), "");
        Rest.post(pseudonymSeed, encryptedPost);
    }

    public static void postProfile(Context context, Profile profile) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String privateKey = preferences.getString(context.getString(R.string.private_key), "");
        String myGroupKey = preferences.getString(context.getString(R.string.group_key), "");

        EncryptedProfile profilePost = new EncryptedProfile(
                profile,
                CryptoUtil.decodePrivateKey(privateKey),
                CryptoUtil.decodeSymmetricKey(myGroupKey));

        String pseudonymSeed = preferences.getString(context.getString(R.string.pseudonym_seed), "");
        Rest.postProfile(pseudonymSeed, profilePost);
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
            // TODO: Possibly switch to snackbar...
            // TODO: Remove UI behavior from this package
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
        Log.v(TAG, String.format("pseudonym is %s", pseudonym));
        Key groupKey = CryptoUtil.decodeSymmetricKey(
                sharedPreferences.getString(context.getString(R.string.group_key), ""));

        PseudonymKey pk = new PseudonymKey(uniqueId, username, pseudonym, groupKey);
        String qrCode = new Encoder().encodeToString(pk.toByteArray());
        Log.v(TAG, "generated QR code: " + qrCode);
        return qrCode;
    }

    public static Profile getProfile(Context context, String username) {
        PseudonymKey friendPK = FriendDatabase.getInstance(context).getKey(username);
        EncryptedProfile encryptedProfile = Rest.getProfile(friendPK.getPseudonym());
        Log.v(TAG, "got profile from network");
        if (encryptedProfile == null) {
            Log.d(TAG, "profile response was null");
            return null;
        }

        Log.v(TAG, String.format("blob is %s", encryptedProfile.getBlob()));

        //--TODO check signature
        return encryptedProfile.decrypt(friendPK.getKey());
    }
}
