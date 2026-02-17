package com.etibe.app.models

import com.etibe.app.utils.ActivateCircleResponse
import com.etibe.app.utils.CircleCreateRequest
import com.etibe.app.utils.CircleCreateResponse
import com.etibe.app.utils.CircleDashboardResponse
import com.etibe.app.utils.DiscoverCirclesResponse
import com.etibe.app.utils.JoinCircleRequest
import com.etibe.app.utils.JoinCircleResponse
import com.etibe.app.utils.LoginRequest
import com.etibe.app.utils.LoginResponse
import com.etibe.app.utils.MyCirclesResponse
import com.etibe.app.utils.RegisterRequest
import com.etibe.app.utils.RegisterResponse
import com.etibe.app.utils.ResendEmailRequest
import com.etibe.app.utils.ResendOtpResponse
import com.etibe.app.utils.UserResponse
import com.etibe.app.utils.VerifyResponse
import com.etibe.app.utils.VerifyEmailRequest
import com.etibe.app.utils.WithdrawOtpRequest
import com.etibe.app.utils.WithdrawOtpResponse
import com.etibe.app.utils.WithdrawRequest
import com.etibe.app.utils.WithdrawResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/v1/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<VerifyResponse>

    @POST("api/v1/auth/resend-otp")
    suspend fun resendOtp(@Body request: ResendEmailRequest): Response<ResendOtpResponse>

    @POST("api/v1/circles")
    suspend fun createCircle(@Body request: CircleCreateRequest): Response<CircleCreateResponse>

    @POST("api/v1/circles/join")
    suspend fun joinCircle(@Body request: JoinCircleRequest): Response<JoinCircleResponse>

    @GET("api/v1/circles")
    suspend fun getMyCircles(): Response<MyCirclesResponse>

    @GET("/api/v1/circles/{id}/dashboard")
    suspend fun getCircleDashboard(@Path("id") id: String): Response<CircleDashboardResponse>

    @POST("api/v1/circles/{id}/activate")
    suspend fun activateCircle(
        @Path("id") id: String,
        @Body body: Map<String, String> = emptyMap()
    ): Response<ActivateCircleResponse>


    @GET("api/v1/circles/discover")
    suspend fun discoverCircles(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<DiscoverCirclesResponse>

    @POST("api/v1/circles/request-withdrawal-otp")
    suspend fun requestWithdrawalOtp(@Body request: WithdrawOtpRequest): Response<WithdrawOtpResponse>

    @POST("api/v1/circles/withdraw")
    suspend fun withdraw(@Body request: WithdrawRequest): Response<WithdrawResponse>

}