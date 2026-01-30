package com.etibe.app.utils

import kotlinx.serialization.Serializable
@Serializable

data class UserResponse(
    val success: Boolean,
    val data: UserData?
)
@Serializable

data class UserData(
    val user: User
)
@Serializable

data class User(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val fullName: String? = "$firstName $lastName",
    val isVerified: Boolean,
    val nearAccountId: String?,
    val walletBalance: WalletBalance?
)
@Serializable

data class WalletBalance(
    val NEAR: String?,
    val USDT: String?,
    val USDC: String?
)