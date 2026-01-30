package com.etibe.app.utils

import kotlinx.serialization.Serializable


@Serializable

data class RegisterRequest(
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val password: String
)

@Serializable
data class RegisterResponse(
    val success: Boolean,
    val data: RegisterData?,
    val meta: Meta? // optional
)

@Serializable

data class RegisterData(
    val message: String,
    val user: User?,
    val requiresVerification: Boolean
)


@Serializable


data class Meta(
    val timestamp: String,
    val path: String,
    val requestId: String
)