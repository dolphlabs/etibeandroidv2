package com.etibe.app

import androidx.annotation.DrawableRes

data class RecentActivity(
    val id: String,
    val title: String,
    val date: String,
    val amount: String,
    val status: String, // "success", "pending", "failed"
    @DrawableRes val iconRes: Int,
    val type: ActivityType
)

enum class ActivityType {
    TOP_UP,
    CONTRIBUTION,
    PAYOUT,
    WITHDRAWAL
}
data class MyEtibe(
    val id: String,
    val name: String,
    val amount: String,
    val frequency: String, // "week", "month", "daily"
    val status: String, // "Active", "Inactive", "Completed"
    val membersCount: Int = 0,
    val nextPayoutDate: String? = null
)