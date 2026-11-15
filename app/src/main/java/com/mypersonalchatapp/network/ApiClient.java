package com.mypersonalchatapp.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    // Needs "adb reverse tcp:3000 tcp:3000" so the device's port 3000 forwards to this computer's server.
    private static final String BASE_URL = "http://localhost:3000/";

    private static ChatApi chatApi;

    private ApiClient() {
    }

    public static ChatApi getChatApi() {
        if (chatApi == null) {
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(3600, TimeUnit.SECONDS)
                    .build();

            chatApi = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ChatApi.class);
        }
        return chatApi;
    }
}
