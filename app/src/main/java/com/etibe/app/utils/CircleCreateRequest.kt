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

data class CircleDetails(
    val id: String,
    val name: String,
    val description: String?,
    val logoUrl: String?,
    val status: String,
    val contributionSettings: CircleCreateRequest.ContributionSettings,
    val memberCount: Int,
    val maxMembers: Int,
    val currentRound: Int,
    val totalRounds: Int,
    val startDate: String,
    val nextPayoutDate: String?,
    val payoutAmount: String?,
    val inviteCode: String?,
    val inviteLink: String?,
    val isPrivate: Boolean,
    val isFull: Boolean,
    val createdAt: String
)