package com.mypersonalchatapp.network;

import com.mypersonalchatapp.models.ChatRequest;
import com.mypersonalchatapp.models.Dto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatApi {

    @POST("Api/Chat/ChatWithModel")
    Call<Dto> chatWithModel(@Body ChatRequest request);
}
