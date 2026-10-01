package com.queueless.app.api;

import android.content.Context;

import com.queueless.app.SessionManager;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        SessionManager sessionManager =
                new SessionManager(context);

        String token = sessionManager.getToken();

        Request.Builder requestBuilder =
                chain.request().newBuilder();

        if (token != null && !token.isEmpty()) {

            requestBuilder.addHeader(
                    "Authorization",
                    "Bearer " + token
            );
        }

        return chain.proceed(
                requestBuilder.build()
        );
    }
}
