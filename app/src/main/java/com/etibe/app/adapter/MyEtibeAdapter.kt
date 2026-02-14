package com.etibe.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.etibe.app.R
import com.etibe.app.databinding.ItemMyEtibeBinding
import com.etibe.app.utils.CircleDetails

class MyEtibeAdapter(
    private val onViewClick: (CircleDetails) -> Unit
) : ListAdapter<CircleDetails, MyEtibeAdapter.GroupViewHolder>(CircleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemMyEtibeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GroupViewHolder(private val binding: ItemMyEtibeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(circle: CircleDetails) {
            binding.apply {
                tvGroupName.text = circle.name
                tvContribution.text = "${circle.contributionSettings.amount} ${circle.contributionSettings.currency}/${circle.contributionSettings.frequency.lowercase()}"
                tvStatus.text = circle.status
                tvStatus.isVisible = circle.status.isNotBlank()
                tvStatus.setBackgroundResource(
                    when (circle.status.uppercase()) {
                        "ACTIVE" -> R.drawable.bg_btn
                        "PENDING" -> R.drawable.bg_status_pending
                        else -> R.drawable.bg_btn
                    }
                )

                btnViewEtibe.setOnClickListener {
                    onViewClick(circle)
                }

                // Optional: show member avatars (you can use Glide/Picasso later)
                // tvMembers.text = "+${circle.memberCount}"
            }
        }
    }

    class CircleDiffCallback : DiffUtil.ItemCallback<CircleDetails>() {
        override fun areItemsTheSame(oldItem: CircleDetails, newItem: CircleDetails): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CircleDetails, newItem: CircleDetails): Boolean {
            return oldItem == newItem
        }
    }
}