package com.queueless.app.api;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("users")
    Call<ResponseBody> register(@Body RegisterRequest request);

    @GET("me")
    Call<ResponseBody> getCurrentUser();

    @GET("services")
    Call<List<ServiceResponse>> getServices();

    @GET("services/my")
    Call<List<ServiceResponse>> getMyServices();

    @POST("services")
    Call<ServiceResponse> createService(@Body ServiceRequest request);

    @POST("tokens")
    Call<TokenResponse> createToken(@Body TokenRequest request);

    @GET("tokens/{tokenId}/queue")
    Call<QueueTokenResponse> getQueuePosition(
            @Path("tokenId") Long tokenId
    );

    // Get all waiting customers for a staff service.
    @GET("tokens/service/{serviceId}/waiting")
    Call<List<TokenResponse>> getWaitingTokens(
            @Path("serviceId") Long serviceId
    );

    @POST("tokens/call-next/{serviceId}")
    Call<TokenResponse> callNext(
            @Path("serviceId") Long serviceId
    );

    @POST("tokens/{tokenId}/start")
    Call<TokenResponse> startServing(
            @Path("tokenId") Long tokenId
    );

    @POST("tokens/{tokenId}/complete")
    Call<TokenResponse> completeToken(
            @Path("tokenId") Long tokenId
    );

    @POST("tokens/{tokenId}/cancel")
    Call<TokenResponse> cancelToken(
            @Path("tokenId") Long tokenId
    );

    @PUT("tokens/{tokenId}/priority")
    Call<TokenResponse> changePriority(
            @Path("tokenId") Long tokenId,
            @Body PriorityRequest request
    );

    @POST("tokens/{tokenId}/no-show")
    Call<TokenResponse> markNoShow(
            @Path("tokenId") Long tokenId
    );


}