package io.islnd.android.islnd.messaging;

import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;
import io.islnd.android.islnd.messaging.server.EventQuery;
import io.islnd.android.islnd.messaging.server.EventQueryResponse;
import io.islnd.android.islnd.messaging.server.ServerTimeResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RestInterface {
    @GET("/ping/")
    Call<Void> getPing(
            @Query("apiKey") String apiKey);

    @POST("/event")
    Call<Void> postEvent(
            @Body EncryptedEvent encryptedEvent,
            @Query("apiKey") String apiKey);

    @POST("/eventQuery")
    Call<EventQueryResponse> postEventQuery(
            @Body EventQuery eventQuery,
            @Query("apiKey") String apiKey);
            
    @GET("/serverTime/")
    Call<ServerTimeResponse> getServerTime(
            @Query("apiKey") String apiKey);
}
