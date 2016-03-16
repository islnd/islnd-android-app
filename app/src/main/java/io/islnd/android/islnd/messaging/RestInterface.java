package io.islnd.android.islnd.messaging;

import io.islnd.android.islnd.messaging.crypto.EncryptedComment;
import io.islnd.android.islnd.messaging.crypto.EncryptedData;
import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;
import io.islnd.android.islnd.messaging.crypto.EncryptedPost;
import io.islnd.android.islnd.messaging.crypto.EncryptedProfile;
import io.islnd.android.islnd.messaging.server.CommentQueryRequest;
import io.islnd.android.islnd.messaging.server.CommentQueryResponse;
import io.islnd.android.islnd.messaging.server.EventQuery;
import io.islnd.android.islnd.messaging.server.EventQueryResponse;
import io.islnd.android.islnd.messaging.server.ProfileResponse;
import io.islnd.android.islnd.messaging.server.PseudonymResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RestInterface {
    @GET("/readers/{username}")
    Call<List<EncryptedData>> readers(
            @Path("username") String username,
            @Query("apiKey") String apiKey);


    @GET("/posts/{pseudonym}")
    Call<List<EncryptedPost>> posts(
            @Path("pseudonym") String pseudonym,
            @Query("apiKey") String apiKey);

    @POST("/post/{pseudonymSeed}")
    Call<Object> post(
            @Path("pseudonymSeed") String pseudonymSeed,
            @Body EncryptedPost encryptedPost,
            @Query("apiKey") String apiKey);


    @POST("/getComments/") // TODO rename to /commentQuery/
    Call<CommentQueryResponse> getComments(
            @Body CommentQueryRequest commentQueryPost,
            @Query("apiKey") String apiKey);

    @POST("/comment/")
    Call<Object> postComment(
            @Body EncryptedComment postCommentRequest,
            @Query("apiKey") String apiKey);

    @GET("/pseudonym/{seed}")
    Call<PseudonymResponse> pseduonym(
            @Path("seed") String seed,
            @Query("apiKey") String apiKey);

    @POST("/publicKey/{username}")
    Call<String> postPublicKey(
            @Path("username") String username,
            @Body String publicKey,
            @Query("apiKey") String apiKey);


    @GET("/profiles/{pseudonym}")
    Call<ProfileResponse> getProfiles(
            @Path("pseudonym") String pseudonym,
            @Query("apiKey") String apiKey);

    @POST("/profile/{pseudonymSeed}")
    Call<Object> postProfile(
            @Path("pseudonymSeed") String pseudonymSeed,
            @Body EncryptedProfile encryptedProfile,
            @Query("apiKey") String apiKey);

    @POST("/event")
    Call<Void> postEvent(
            @Body EncryptedEvent encryptedEvent,
            @Query("apiKey") String apiKey);

    @POST("/eventQuery")
    Call<EventQueryResponse> postEventQuery(
            @Body EventQuery eventQuery,
            @Query("apiKey") String apiKey);
}
