package org.island.messaging;

import java.io.IOException;
import java.util.List;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by poo on 2/13/16.
 */
public class Rest {

    private final static String HOST = "http://ec2-54-152-254-52.compute-1.amazonaws.com:1935";

    public static List<EncryptedPseudonymKey> getReaders(String username) {
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
}