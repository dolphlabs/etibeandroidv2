package com.etibe.app.utils

data class WithdrawOtpRequest(
    val amount: String,
    val asset: String
)

data class WithdrawRequest(
    val amount: String,
    val asset: String,
    val destinationAddress: String,
    val otp: String
)

data class WithdrawData(
    val message: String?,
    val transactionId: String?,
    val status: String?,
    val estimatedCompletionTime: String?
)
data class WithdrawOtpResponse(
    val success: Boolean,
    val data: WithdrawOtpData?,
    val meta: Meta?
)

data class WithdrawOtpData(
    val success: Boolean,
    val message: String?
)
data class WithdrawResponse(
    val success: Boolean,
    val data: WithdrawData?,
    val meta: Meta?
)