package com.etibe.app.utils

data class GenericResponse(
    val success: Boolean,
    val message: String?,
    val data: Any?,
    val error: ApiError?
)

data class InviteRequest(
    val inviteeEmail: String,
    val circleId: String
)