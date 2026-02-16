package com.etibe.app.utils

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────
// My Circles Response (GET /circles)
// ─────────────────────────────────────────────

data class MyCirclesResponse(
    val success: Boolean,
    val data: MyCirclesData?,
    val meta: Meta?
)

data class MyCirclesData(
    val data: List<CircleDetails>?
)

// ─────────────────────────────────────────────
// Circle Dashboard Response (GET /circles/:id/dashboard)
// ─────────────────────────────────────────────

data class CircleDashboardResponse(
    val success: Boolean,
    val data: DashboardData?,
    val error: ApiError?,              // ← add this for failure cases
    val meta: Meta?
)

data class DashboardData(
    val data: DashboardDetails?
)

data class DashboardDetails(
    val circle: Circle?,
    val stats: Stats?,
    val nextRecipient: NextRecipient?,
    @SerializedName("daysUntilNextContribution")
    val daysUntilNextContribution: Int?,
    @SerializedName("payoutOrder")
    val payoutOrder: List<PayoutOrderMember>?,
    @SerializedName("recentActivity")
    val recentActivity: List<Any>?,           // refine later if needed
    @SerializedName("userContributedThisRound")
    val userContributedThisRound: Boolean?
)

// ─────────────────────────────────────────────
// Shared Models
// ─────────────────────────────────────────────

data class CircleDetails(
    val id: String?,
    val name: String?,
    val description: String?,
    @SerializedName("logoUrl")
    val logoUrl: String?,
    val status: String?,
    @SerializedName("contributionSettings")
    val contributionSettings: ContributionSettings?,
    @SerializedName("memberCount")
    val memberCount: Int?,
    @SerializedName("maxMembers")
    val maxMembers: Int?,
    @SerializedName("currentRound")
    val currentRound: Int?,
    @SerializedName("totalRounds")
    val totalRounds: Int?,
    @SerializedName("startDate")
    val startDate: String?,
    @SerializedName("nextPayoutDate")
    val nextPayoutDate: String?,
    @SerializedName("payoutAmount")
    val payoutAmount: String?,
    @SerializedName("inviteCode")
    val inviteCode: String?,
    @SerializedName("inviteLink")
    val inviteLink: String?,
    @SerializedName("isPrivate")
    val isPrivate: Boolean?,
    @SerializedName("isFull")
    val isFull: Boolean?,
    @SerializedName("createdAt")
    val createdAt: String?
)

data class ContributionSettings(
    @SerializedName("amount")
    val amount: Any?,               // can be String or {"$numberDecimal": "100"}

    val currency: String?,

    val frequency: String?,

    @SerializedName("gracePeriodDays")
    val gracePeriodDays: Int?,

    @SerializedName("penaltyPercentage")
    val penaltyPercentage: Any?     // can be String or {"$numberDecimal": "10.25"}
)

data class Stats(
    val collected: String?,
    val target: String?,
    val progress: Int?,
    @SerializedName("membersContributed")
    val membersContributed: Int?,
    @SerializedName("totalMembers")
    val totalMembers: Int?
)

data class NextRecipient(
    @SerializedName("userId")
    val userId: String?,
    val username: String?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("lastName")
    val lastName: String?,
    val position: Int?
)

data class PayoutOrderMember(
    @SerializedName("userId")
    val userId: String?,
    val username: String?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("lastName")
    val lastName: String?,
    val position: Int?,
    @SerializedName("hasReceivedPayout")
    val hasReceivedPayout: Boolean?,
    @SerializedName("joinedAt")
    val joinedAt: String?,
    val status: String?
)
