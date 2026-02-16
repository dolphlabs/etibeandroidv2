package com.etibe.app.utils


import com.google.gson.annotations.SerializedName

data class DiscoverCirclesResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: DiscoverCirclesData,
    @SerializedName("meta")
    val meta: ResponseMeta
)

data class DiscoverCirclesData(
    @SerializedName("data")
    val circles: List<Circle>
)

data class Circle(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("logoUrl")
    val logoUrl: String?,
    @SerializedName("status")
    val status: String, // RECRUITING, ACTIVE, COMPLETED, etc.
    @SerializedName("contractAddress")
    val contractAddress: String,
    @SerializedName("contributionSettings")
    val contributionSettings: ContributionSettings,
    @SerializedName("memberCount")
    val memberCount: Int,
    @SerializedName("maxMembers")
    val maxMembers: Int,
    @SerializedName("currentRound")
    val currentRound: Int,
    @SerializedName("totalRounds")
    val totalRounds: Int,
    @SerializedName("startDate")
    val startDate: String,
    @SerializedName("nextPayoutDate")
    val nextPayoutDate: String?,
    @SerializedName("payoutAmount")
    val payoutAmount: String,
    @SerializedName("inviteCode")
    val inviteCode: String,
    @SerializedName("inviteLink")
    val inviteLink: String,
    @SerializedName("isPrivate")
    val isPrivate: Boolean,
    @SerializedName("isFull")
    val isFull: Boolean,
    @SerializedName("createdAt")
    val createdAt: String
) {
    fun getFormattedContribution(): String {
        val amount = contributionSettings.amount
        val currency = when (contributionSettings.currency) {
            "USDT" -> "$"
            "NGN", "NAIRA" -> "₦"
            else -> contributionSettings.currency
        }
        val frequency = when (contributionSettings.frequency) {
            "WEEKLY" -> "week"
            "BI_WEEKLY" -> "2 weeks"
            "MONTHLY" -> "month"
            "DAILY" -> "day"
            else -> contributionSettings.frequency?.lowercase() ?: ""
        }
        return "$currency$amount/$frequency"
    }

    fun getStatusText(): String {
        return when (status) {
            "RECRUITING" -> "Recruiting"
            "ACTIVE" -> "Active"
            "COMPLETED" -> "Completed"
            "PAUSED" -> "Paused"
            else -> status.replaceFirstChar { it.uppercase() }
        }
    }

    fun getStatusColor(): String {
        return when (status) {
            "RECRUITING" -> "#FFA500" // Orange
            "ACTIVE" -> "#2AD167" // Green
            "COMPLETED" -> "#6B7280" // Gray
            "PAUSED" -> "#FFC107" // Yellow
            else -> "#6B7280"
        }
    }

    fun getBackgroundColor(): String {
        return when (status) {
            "RECRUITING" -> "#FFF4E5" // Light Orange
            "ACTIVE" -> "#E8F5E9" // Light Green
            "COMPLETED" -> "#F5F5F5" // Light Gray
            "PAUSED" -> "#FFF9E5" // Light Yellow
            else -> "#F5F5F5"
        }
    }

    fun getRemainingSlots(): Int {
        return maxMembers - memberCount
    }

    fun getMemberCountText(): String {
        return if (memberCount > 3) {
            "+${memberCount - 3}"
        } else {
            ""
        }
    }
}

