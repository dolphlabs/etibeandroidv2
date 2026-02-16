package com.etibe.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.etibe.app.R
import com.etibe.app.databinding.ItemRecentActivityBinding
import com.etibe.app.utils.ActivityType
import com.etibe.app.utils.RecentActivity

class RecentActivityAdapter(
    private val onItemClick: (RecentActivity) -> Unit = {}
) : ListAdapter<RecentActivity, RecentActivityAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemRecentActivityBinding,
        private val onItemClick: (RecentActivity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: RecentActivity) {
            binding.apply {
                tvTitle.text = activity.title
                tvDate.text = activity.date
                tvAmount.text = activity.amount
                tvStatus.text = activity.status
                ivIcon.setImageResource(activity.iconRes)

                // Set icon background color based on type
                val iconBgColor = when (activity.type) {
                    ActivityType.TOP_UP -> R.color.icon_bg_green
                    ActivityType.CONTRIBUTION -> R.color.icon_bg_red
                    ActivityType.PAYOUT -> R.color.icon_bg_yellow
                    ActivityType.WITHDRAWAL -> R.color.icon_bg_gray
                }
                cvIcon.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, iconBgColor)
                )

                // Set status color
                val statusColor = when (activity.status.lowercase()) {
                    "success" -> R.color.status_success
                    "pending" -> R.color.status_pending
                    "failed" -> R.color.status_failed
                    else -> R.color.text_secondary
                }
                tvStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, statusColor)
                )

                // Set click listener
                root.setOnClickListener {
                    onItemClick(activity)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<RecentActivity>() {
        override fun areItemsTheSame(oldItem: RecentActivity, newItem: RecentActivity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecentActivity, newItem: RecentActivity): Boolean {
            return oldItem == newItem
        }
    }
}