package com.etibe.app.utils

data class ActivateCircleResponse(
    val success: Boolean,
    val data: ActivateData?,
    val meta: Meta?
)

data class ActivateData(
    val message: String?,
    val data: CircleDetails?,
    val contractAddress: String?
)