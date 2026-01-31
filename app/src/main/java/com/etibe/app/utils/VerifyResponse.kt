package com.etibe.app.utils

import kotlinx.serialization.Serializable

@Serializable

data class VerifyResponse(
    val success: Boolean,
    val data: VerifyData?,
    val meta: Meta?
)
@Serializable

data class VerifyData(
    val message: String,
    val user: User?,
    val nearAccountId: String?,
    val onboardingCompleted: Boolean
)
@Serializable

data class ResendOtpResponse(
    val success: Boolean,
    val data: ResendData?,
    val meta: Meta?
)
@Serializable

data class ResendData(
    val success: Boolean,
    val message: String
)
data class VerifyEmailRequest(
    val email: String,
    val otp: String
)