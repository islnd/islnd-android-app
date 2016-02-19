package org.island.messaging;

import android.util.Log;

import org.island.messaging.crypto.EncryptedData;
import org.island.messaging.server.PseudonymResponse;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Rest {
    private static final String TAG = Rest.class.getSimpleName();

    private final static String HOST = "http://ec2-54-152-104-67.compute-1.amazonaws.com:1935";

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

    public static List<EncryptedData> getPosts(String pseudonym) {
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

    public static void post(String pseudonymSeed, String encryptedPost) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            Response<Object> result = service.post(
                    pseudonymSeed,
                    new EncryptedData(encryptedPost)).execute();
            Log.v(TAG, "made a post");
            Log.v(TAG, "response code " + result.code());
            //--TODO check that post was successful
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void postProfile(String pseudonymSeed, EncryptedData profilePost) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            Response<Object> result = service.postProfile(
                    pseudonymSeed,
                    profilePost).execute();
            Log.v(TAG, "posted profile");
            Log.v(TAG, "response code " + result.code());
            //--TODO check that post was successful
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPseudonym(String seed) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);
        try {
            Response<PseudonymResponse> result = service.pseduonym(seed).execute();
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

    public static EncryptedData getProfile(String pseudonym) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);
        try {
            Response<EncryptedData> result = service.getProfile(pseudonym).execute();
            if (result.code() == 200) {
                return result.body();
            }
            else {
                Log.d(TAG, "/profile GET returned code " + result.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
