package org.island.messaging;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RestInterface
{
    @GET("/readers/{username}")
    Call<List<EncryptedPseudonymKey>> readers(
            @Path("username") String username);

    @GET("/posts/{pseudonym}")
    Call<List<EncryptedPost>> posts(
            @Path("pseudonym") String pseudonym);

    @GET("/pseudonym/{seed}")
    Call<String> pseduonym(
            @Path("seed") String seed);

    @POST("/publicKey/{username}")
    Call<String> postPublicKey(@Path("username") String username, @Body String publicKey);

    @POST("/post/{pseudonymSeed}")
    Call<Object> post(@Path("pseudonymSeed") String pseudonymSeed, @Body EncryptedPost encryptedPost);
}
