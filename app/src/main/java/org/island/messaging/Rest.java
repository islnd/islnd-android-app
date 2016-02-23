package org.island.messaging;

import android.util.Log;

import org.island.messaging.crypto.EncryptedData;
import org.island.messaging.crypto.EncryptedPost;
import org.island.messaging.crypto.EncryptedProfile;
import org.island.messaging.server.ProfileResponse;
import org.island.messaging.server.PseudonymResponse;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Rest {
    private static final String TAG = Rest.class.getSimpleName();

    private final static String HOST = "https://islnd.io:1935";

    public static List<EncryptedData> getReaders(String username, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            return service.readers(username, apiKey).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void postPublicKey(String username, String publicKey, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            Response<String> response = service.postPublicKey(username, publicKey, apiKey).execute();
            if (response.code() != 200) {
                Log.v(TAG, "/publicKey POST returned code " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<EncryptedPost> getPosts(String pseudonym, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            return service.posts(pseudonym, apiKey).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void post(String pseudonymSeed, EncryptedPost encryptedPost, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            //--TODO check that post was successful
            service.post(pseudonymSeed, encryptedPost, apiKey).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void postProfile(String pseudonymSeed, EncryptedProfile profilePost, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            //--TODO check that post was successful
            service.postProfile(pseudonymSeed, profilePost, apiKey).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPseudonym(String seed, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);
        try {
            Response<PseudonymResponse> result = service.pseduonym(seed, apiKey).execute();
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

    public static List<EncryptedProfile> getProfiles(String pseudonym, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);
        try {
            Response<ProfileResponse> response = service.getProfiles(pseudonym, apiKey).execute();
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
