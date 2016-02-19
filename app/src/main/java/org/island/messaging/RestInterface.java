package org.island.messaging;

import org.island.messaging.crypto.EncryptedData;
import org.island.messaging.crypto.EncryptedPost;
import org.island.messaging.crypto.EncryptedProfile;
import org.island.messaging.server.ProfileResponse;
import org.island.messaging.server.PseudonymResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RestInterface
{
    @GET("/readers/{username}")
    Call<List<EncryptedData>> readers(
            @Path("username") String username);


    @GET("/posts/{pseudonym}")
    Call<List<EncryptedPost>> posts(@Path("pseudonym") String pseudonym);

    @POST("/post/{pseudonymSeed}")
    Call<Object> post(@Path("pseudonymSeed") String pseudonymSeed, @Body EncryptedPost encryptedPost);


    @GET("/pseudonym/{seed}")
    Call<PseudonymResponse> pseduonym(@Path("seed") String seed);

    @POST("/publicKey/{username}")
    Call<String> postPublicKey(@Path("username") String username, @Body String publicKey);


    @GET("/profiles/{pseudonym}")
    Call<ProfileResponse> getProfiles(@Path("pseudonym") String pseudonym);

    @POST("/profile/{pseudonymSeed}")
    Call<Object> postProfile(
            @Path("pseudonymSeed") String pseudonymSeed,
            @Body EncryptedProfile encryptedProfile);
}
