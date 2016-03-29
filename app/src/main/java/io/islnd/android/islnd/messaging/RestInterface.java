package io.islnd.android.islnd.messaging;


import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;
import io.islnd.android.islnd.messaging.crypto.EncryptedMessage;
import io.islnd.android.islnd.messaging.crypto.EncryptedResource;
import io.islnd.android.islnd.messaging.server.EventQuery;
import io.islnd.android.islnd.messaging.server.EventQueryResponse;
import io.islnd.android.islnd.messaging.server.MessageQuery;
import io.islnd.android.islnd.messaging.server.MessageQueryResponse;
import io.islnd.android.islnd.messaging.server.ResourceQuery;
import io.islnd.android.islnd.messaging.server.ResourceQueryResponse;
import io.islnd.android.islnd.messaging.server.ServerTimeResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RestInterface {

    static final String API_KEY_PARAM = "apiKey";

    @GET("/ping/")
    Call<Void> getPing(
            @Query(API_KEY_PARAM) String apiKey);

    @POST("/resource")
    Call<Void> postResource(
            @Body EncryptedResource encryptedResource,
            @Query(API_KEY_PARAM) String apiKey);

    @POST("/resourceQuery")
    Call<ResourceQueryResponse> postResourceQuery(
            @Body ResourceQuery resourceQuery,
            @Query(API_KEY_PARAM) String apiKey);

    @POST("/event")
    Call<Void> postEvent(
            @Body EncryptedEvent encryptedEvent,
            @Query(API_KEY_PARAM) String apiKey);

    @POST("/eventQuery")
    Call<EventQueryResponse> postEventQuery(
            @Body EventQuery eventQuery,
            @Query(API_KEY_PARAM) String apiKey);
            
    @GET("/serverTime/")
    Call<ServerTimeResponse> getServerTime(
            @Query(API_KEY_PARAM) String apiKey);

    @POST("/message")
    Call<Void> postMessage(
            @Body EncryptedMessage encryptedMessage,
            @Query(API_KEY_PARAM) String apiKey);

    @POST("/messageQuery")
    Call<MessageQueryResponse> postMessageQuery(
            @Body MessageQuery messageQuery,
            @Query(API_KEY_PARAM) String apiKey);
}
