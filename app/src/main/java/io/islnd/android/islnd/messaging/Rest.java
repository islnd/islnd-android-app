package io.islnd.android.islnd.messaging;

import android.util.Log;

import io.islnd.android.islnd.messaging.crypto.EncryptedData;
import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;
import io.islnd.android.islnd.messaging.interceptor.DelayInterceptor;
import io.islnd.android.islnd.messaging.server.EventQuery;
import io.islnd.android.islnd.messaging.server.EventQueryResponse;
import io.islnd.android.islnd.messaging.server.PseudonymResponse;
import io.islnd.android.islnd.messaging.server.ServerTimeResponse;

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

    public static String getPseudonym(String seed, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);
        try {
            Response<PseudonymResponse> result = service.pseduonym(seed, apiKey).execute();
            if (result.code() == HTTP_OK) {
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

    public static Boolean getPing(String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);
        try {
            Response<Void> result = service.getPing(apiKey).execute();
            if (result.code() == HTTP_OK) {
                return true;
            }
            else {
                Log.d(TAG, "/ping GET returned code " + result.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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

    public static long getServerTimeOffsetMillis(int repetitions, String apiKey) throws IOException {
        // account for network delay by using OkHttpClient's Interceptor
        DelayInterceptor delayInterceptor = new DelayInterceptor();
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .addNetworkInterceptor(delayInterceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        // record lowest delay for all attempts
        long minNetworkDelayNanos = Long.MAX_VALUE;
        long mostAccurateOffsetMillis = 0;

        for (int i = 0; i < repetitions; i++) {
            Log.d(TAG, "testing network delay for server time");
            try {
                // make call, which will update delayInterceptor
                Response<ServerTimeResponse> response = service.getServerTime(apiKey).execute();
                if (response.code() != HTTP_OK) {
                    continue;
                }

                long networkDelayNanos = delayInterceptor.getNetworkDelayNanos();
                if (networkDelayNanos < minNetworkDelayNanos) {
                    minNetworkDelayNanos = networkDelayNanos;

                    long serverTimeMillis = Long.parseLong(response.body().getServerTime());
                    long networkDelayMillis = networkDelayNanos / 1000000;
                    mostAccurateOffsetMillis = (serverTimeMillis - networkDelayMillis) - delayInterceptor.getRequestTimeMillis();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // if unchanged, we never made a successful connection
        if (minNetworkDelayNanos == Long.MAX_VALUE) {
            throw new IOException("Unable to make a successful connection to server.");
        }

        return mostAccurateOffsetMillis;
    }
}
