package io.islnd.android.islnd.messaging.interceptor;

import java.io.IOException;

public class DelayInterceptor implements okhttp3.Interceptor {
    private long requestTimeMillis;
    private long networkDelayNanos;
    public long getRequestTimeMillis() {
        return requestTimeMillis;
    }
    public long getNetworkDelayNanos() {
        return networkDelayNanos;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        networkDelayNanos = 0;
        requestTimeMillis = System.currentTimeMillis();

        // perform request, and get delay
        long t1 = System.nanoTime();
        okhttp3.Response response = chain.proceed(chain.request());
        long t2 = System.nanoTime();

        // assume half of round trip
        networkDelayNanos = (t2 - t1) / 2;

        return response;
    }
}
