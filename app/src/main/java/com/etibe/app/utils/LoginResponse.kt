package com.etibe.app.utils

// 1. Request DTO
data class LoginRequest(
    val identifier: String,
    val password: String
)

// 2. Response DTO (based on your real response)
data class LoginResponse(
    val success: Boolean,
    val data: LoginData?,
)

data class LoginData(
    val message: String,
    val user: User
)

data class User(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val fullName: String?,
    val isVerified: Boolean,
    val nearAccountId: String?
)

// 3. In your Fragment
