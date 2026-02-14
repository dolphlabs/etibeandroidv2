package com.etibe.app.utils

data class CircleCreateRequest(
    val name: String,
    val description: String? = null,
    val logoUrl: String? = null,
    val contributionSettings: ContributionSettings,
    val maxMembers: Int? = null,
    val startDate: String? = null,
    val isPrivate: Boolean = false
) {
    data class ContributionSettings(
        val amount: String,
        val currency: String,
        val frequency: String,
        val gracePeriodDays: Int,
        val penaltyPercentage: String? = null
    )
}

data class CircleCreateResponse(
    val success: Boolean,
    val data: CircleData?,
    val meta: Meta?
)

data class CircleData(
    val message: String?,
    val data: CircleDetails?
)

