package com.etibe.app.utils

data class JoinCircleRequest(
    val inviteCode: String
)

data class JoinCircleResponse(
    val success: Boolean,
    val data: JoinData?,
    val meta: Meta?
)

data class JoinData(
    val message: String,
    val data: CircleDetails?
)

