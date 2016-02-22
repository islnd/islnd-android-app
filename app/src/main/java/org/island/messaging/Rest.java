package org.island.messaging;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.island.island.R;

import org.island.messaging.crypto.EncryptedData;
import org.island.messaging.crypto.EncryptedPost;
import org.island.messaging.crypto.EncryptedProfile;
import org.island.messaging.server.ProfileResponse;
import org.island.messaging.server.PseudonymResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Rest {
    private static final String TAG = Rest.class.getSimpleName();

    private final static String HOST = "https://ec2-54-152-104-67.compute-1.amazonaws.com:1935";
    private static String mPassword = null;

    public static List<EncryptedData> getReaders(Context context, String username) {
        Retrofit retrofit = getGsonRetrofit(context);
        RestInterface service = retrofit.create(RestInterface.class);

        try {
            return service.readers(username).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String postPublicKey(Context context, String username, String publicKey) {
        Retrofit retrofit = getScalarRetrofit(context);
        RestInterface service = retrofit.create(RestInterface.class);

        try {
            service.postPublicKey(username, publicKey).execute().body();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<EncryptedPost> getPosts(Context context, String pseudonym) {
        Retrofit retrofit = getGsonRetrofit(context);
        RestInterface service = retrofit.create(RestInterface.class);

        try {
            return service.posts(pseudonym).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void post(Context context, String pseudonymSeed, EncryptedPost encryptedPost) {
        Retrofit retrofit = getGsonRetrofit(context);
        RestInterface service = retrofit.create(RestInterface.class);

        try {
            //--TODO check that post was successful
            service.post(pseudonymSeed, encryptedPost).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void postProfile(Context context, String pseudonymSeed, EncryptedProfile profilePost) {
        Retrofit retrofit = getGsonRetrofit(context);
        RestInterface service = retrofit.create(RestInterface.class);

        try {
            //--TODO check that post was successful
            service.postProfile(pseudonymSeed, profilePost).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPseudonym(Context context, String seed) {
        Retrofit retrofit = getGsonRetrofit(context);
        RestInterface service = retrofit.create(RestInterface.class);

        try {
            Response<PseudonymResponse> result = service.pseduonym(seed).execute();
            if (result.code() == 200) {
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

    public static List<EncryptedProfile> getProfiles(Context context, String pseudonym) {
        Retrofit retrofit = getGsonRetrofit(context);
        RestInterface service = retrofit.create(RestInterface.class);
        try {
            Response<ProfileResponse> response = service.getProfiles(pseudonym).execute();
            if (response.code() == 200) {
                return response.body().getProfiles();
            }
            else {
                Log.d(TAG, "/profile GET returned code " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static OkHttpClient buildHttpsClient(Context context) {
        try {
            KeyStore keyStore = readKeyStore(context);
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            String password = getPassword(context);
            Log.v(TAG, "password is " + password);
            keyManagerFactory.init(keyStore, password.toCharArray());
            sslContext.init(
                    keyManagerFactory.getKeyManagers(),
                    trustManagerFactory.getTrustManagers(),
                    new SecureRandom());

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return null;
    }

    @NonNull
    private static String getPassword(Context context) {
        if (mPassword == null) {
            mPassword = PreferenceManager.getDefaultSharedPreferences(context).
                    getString(context.getString(R.string.keystore_password_key), "");
        }

        return mPassword;
    }

    private static KeyStore readKeyStore(Context context) {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        char[] password = getPassword(context).toCharArray();
        InputStream fis = null;
        try {
            fis = context.getResources().openRawResource(R.raw.island);
            ks.load(fis, password);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return ks;
    }

    @NonNull
    private static Retrofit getGsonRetrofit(Context context) {
        return new Retrofit.Builder()
                .client(buildHttpsClient(context))
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @NonNull
    private static Retrofit getScalarRetrofit(Context context) {
        return new Retrofit.Builder()
                .client(buildHttpsClient(context))
                .baseUrl(HOST)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }
}
