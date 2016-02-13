package org.island.messaging;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by poo on 2/13/16.
 */
public interface RestInterface
{
    @GET("/readers/{username}")
    Call<List<EncryptedPseudonymKey>> readers(
            @Path("username") String username);

    @POST("/publicKey/{username}")
    Call<String> postPublicKey(@Path("username") String username, @Body String publicKey);
}
