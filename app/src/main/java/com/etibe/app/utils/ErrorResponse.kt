package com.etibe.app.utils

// ErrorResponse.kt
data class ErrorResponse(
    val success: Boolean,
    val data: Any?,
    val error: ApiError?,
    val meta: Meta?
)

data class ApiError(
    val code: String?,
    val message: String?,
    val details: ErrorDetails?
)

data class ErrorDetails(
    val validationErrors: List<String>?
)