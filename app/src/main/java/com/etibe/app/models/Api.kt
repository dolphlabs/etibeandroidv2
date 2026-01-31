package com.etibe.app.models

import com.etibe.app.utils.LoginRequest
import com.etibe.app.utils.LoginResponse
import com.etibe.app.utils.RegisterRequest
import com.etibe.app.utils.RegisterResponse
import com.etibe.app.utils.ResendOtpResponse
import com.etibe.app.utils.UserResponse
import com.etibe.app.utils.VerifyResponse
import com.etibe.app.utils.VerifyEmailRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("/api/v1/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<VerifyResponse>

    @POST("/api/v1/auth/resend-otp")
    suspend fun resendOtp(): Response<ResendOtpResponse>

}