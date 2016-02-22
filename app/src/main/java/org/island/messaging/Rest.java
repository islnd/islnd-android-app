package org.island.messaging;

import android.content.Context;
import android.util.Log;

import org.island.messaging.crypto.EncryptedData;
import org.island.messaging.crypto.EncryptedPost;
import org.island.messaging.crypto.EncryptedProfile;
import org.island.messaging.server.ProfileResponse;
import org.island.messaging.server.PseudonymResponse;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Rest {
    private static final String TAG = Rest.class.getSimpleName();

    private final static String HOST = "https://islnd.io:1935";

    public static List<EncryptedData> getReaders(String username) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            return service.readers(username).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String postPublicKey(String username, String publicKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            service.postPublicKey(username, publicKey).execute().body();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<EncryptedPost> getPosts(String pseudonym) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            return service.posts(pseudonym).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void post(String pseudonymSeed, EncryptedPost encryptedPost) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            //--TODO check that post was successful
            service.post(pseudonymSeed, encryptedPost).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void postProfile(String pseudonymSeed, EncryptedProfile profilePost) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            //--TODO check that post was successful
            service.postProfile(pseudonymSeed, profilePost).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPseudonym(Context context, String seed) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);
        String key = "secret";
        try {
            Response<PseudonymResponse> result = service.pseduonym(seed, key).execute();
            if (result.code() == 200) {
                return result.body().getPseudonym();
            }
            else {
                Log.d(TAG, "/pseudonym GET returned code " + result.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<EncryptedProfile> getProfiles(String pseudonym) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);
        try {
            Response<ProfileResponse> response = service.getProfiles(pseudonym).execute();
            if (response.code() == 200) {
                return response.body().getProfiles();
            }
            else {
                Log.d(TAG, "/profile GET returned code " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
