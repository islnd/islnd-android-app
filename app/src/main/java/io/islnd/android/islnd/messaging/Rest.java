package io.islnd.android.islnd.messaging;

import android.util.Log;

import io.islnd.android.islnd.messaging.crypto.EncryptedData;
import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;
import io.islnd.android.islnd.messaging.server.EventQuery;
import io.islnd.android.islnd.messaging.server.EventQueryResponse;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Rest {
    private static final String TAG = Rest.class.getSimpleName();

    private final static String HOST = "https://islnd.io:1935";
    private static final int HTTP_OK = 200;

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
            if (response.code() != HTTP_OK) {
                Log.v(TAG, "/publicKey POST returned code " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int postEvent(EncryptedEvent encryptedEvent, String apiKey) throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            Response<Void> response = service.postEvent(encryptedEvent, apiKey).execute();

            if (response.code() != HTTP_OK) {
                Log.d(TAG, "post event returned code" + response.code());
            }

            return response.code();
        } catch (IOException e) {
            throw e;
        }
    }

    public static List<EncryptedEvent> postEventQuery(EventQuery eventQuery, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            Response<EventQueryResponse> response = service.postEventQuery(
                    eventQuery,
                    apiKey).execute();
            if (response.code() == HTTP_OK) {
                Log.v(TAG, response.body().getEvents().size() + " events");
                return response.body().getEvents();
            }
            else {
                Log.d(TAG, "post event query returned code" + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
