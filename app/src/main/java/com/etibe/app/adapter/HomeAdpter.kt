package com.etibe.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.etibe.app.utils.MyEtibe
import com.etibe.app.R
import com.etibe.app.databinding.ItemMyEtibeBinding

class HomeAdpter(
    private val onItemClick: (MyEtibe) -> Unit = {}
) : ListAdapter<MyEtibe, HomeAdpter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMyEtibeBinding.inflate(
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
        private val binding: ItemMyEtibeBinding,
        private val onItemClick: (MyEtibe) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(etibe: MyEtibe) {
            binding.apply {
                tvGroupName.text = etibe.name
                tvContribution.text = "${etibe.amount}/${etibe.frequency}"
                tvStatus.text = etibe.status

                // Set status styling
                val (bgColor, textColor) = when (etibe.status.lowercase()) {
                    "active" -> Pair(R.color.status_active_bg, R.color.status_active_text)
                    "inactive" -> Pair(R.color.status_inactive_bg, R.color.status_inactive_text)
                    "completed" -> Pair(R.color.status_completed_bg, R.color.status_completed_text)
                    else -> Pair(R.color.status_inactive_bg, R.color.text_secondary)
                }

                val drawable = ContextCompat.getDrawable(
                    binding.root.context,
                    R.drawable.bg_auth_button
                )?.mutate()

                drawable?.setTint(ContextCompat.getColor(binding.root.context, bgColor))
                tvStatus.background = drawable

                tvStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, textColor)
                )

                // Set click listener
                root.setOnClickListener {
                    onItemClick(etibe)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<MyEtibe>() {
        override fun areItemsTheSame(oldItem: MyEtibe, newItem: MyEtibe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MyEtibe, newItem: MyEtibe): Boolean {
            return oldItem == newItem
        }
    }
}