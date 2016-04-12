package io.islnd.android.islnd.messaging;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;
import io.islnd.android.islnd.messaging.crypto.EncryptedMessage;
import io.islnd.android.islnd.messaging.crypto.EncryptedResource;
import io.islnd.android.islnd.messaging.interceptor.DelayInterceptor;
import io.islnd.android.islnd.messaging.server.EventQuery;
import io.islnd.android.islnd.messaging.server.EventQueryResponse;
import io.islnd.android.islnd.messaging.server.MessageQuery;
import io.islnd.android.islnd.messaging.server.MessageQueryResponse;
import io.islnd.android.islnd.messaging.server.ResourceQuery;
import io.islnd.android.islnd.messaging.server.ResourceQueryResponse;
import io.islnd.android.islnd.messaging.server.ServerTimeResponse;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Rest {
    private static final String TAG = Rest.class.getSimpleName();

    private final static String HOST = "https://islnd.io:1935";
    private static final int HTTP_OK = 200;

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

    public static void postMessage(EncryptedMessage encryptedMessage, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            Response<Void> response = service.postMessage(encryptedMessage, apiKey).execute();

            if (response.code() != HTTP_OK) {
                Log.d(TAG, "post message returned code" + response.code());
            }
            else {
                Log.v(TAG, "post message to mailbox " + encryptedMessage.getMailbox());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void postResource(EncryptedResource encryptedResource, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            Log.v(TAG, "posting resource with key " + encryptedResource.getResourceKey());
            Response<Void> response = service.postResource(encryptedResource, apiKey).execute();

            if (response.code() != HTTP_OK) {
                Log.d(TAG, "post resource returned code" + response.code());
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

    public static List<EncryptedMessage> postMessageQuery(MessageQuery messageQuery, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            Log.v(TAG, "checking messages " + messageQuery);
            Response<MessageQueryResponse> response = service.postMessageQuery(
                    messageQuery,
                    apiKey).execute();
            if (response.code() == HTTP_OK) {
                Log.v(TAG, response.body().getMessages().size() + " messages");
                return response.body().getMessages();
            }
            else {
                Log.d(TAG, "post event query returned code" + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<EncryptedResource> postResourceQuery(ResourceQuery resourceQuery, String apiKey) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RestInterface service = retrofit.create(RestInterface.class);

        try {
            Log.v(TAG, "getting resources " + resourceQuery);
            Response<ResourceQueryResponse> response = service.postResourceQuery(
                    resourceQuery,
                    apiKey).execute();
            if (response.code() == HTTP_OK) {
                Log.v(TAG, response.body().getResources().size() + " resources");
                return response.body().getResources();
            }
            else {
                Log.d(TAG, "post resource query returned code" + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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
