package com.etibe.app.utils

data class UserResponse(
    val success: Boolean,
    val data: UserData?
)

data class UserData(
    val user: User
)

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

data class WalletBalance(
    val NEAR: String?,
    val USDT: String?,
    val USDC: String?
)