package com.queueless.app.api;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    public static Retrofit getInstance(Context context) {

        if (retrofit == null) {

            OkHttpClient client =
                    new OkHttpClient.Builder()
                            .addInterceptor(
                                    new AuthInterceptor(context)
                            )
                            .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://127.0.0.1:8080/")
                    .client(client)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        }

        return retrofit;
    }
}
