package com.etibe.app.utils

// 1. Request DTO
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val success: Boolean,
    val data: LoginData? = null,      // nullable in case success = false
    val meta: ResponseMeta? = null
)

@Serializable
data class LoginData(
    val message: String,
    val user: User,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class User(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val isVerified: Boolean,
    val nearAccountId: String
)

@Serializable
data class ResponseMeta(
    val timestamp: String,     // ISO 8601 string
    val path: String,
    val requestId: String
)
@Serializable

data class LoginRequest(
    val identifier: String,
    val password: String


)