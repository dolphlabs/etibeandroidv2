package com.etibe.app.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.etibe.app.databinding.ItemCircleCardBinding
import com.etibe.app.utils.Circle

class CirclesAdapter(
    private val onCircleClick: (Circle) -> Unit,
    private val onJoinClick: (Circle) -> Unit
) : ListAdapter<Circle, CirclesAdapter.CircleViewHolder>(CircleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CircleViewHolder {
        val binding = ItemCircleCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CircleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CircleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CircleViewHolder(
        private val binding: ItemCircleCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(circle: Circle) {
            binding.apply {
                // Set circle name
                tvCircleName.text = circle.name

                // Set contribution amount
                tvContribution.text = circle.getFormattedContribution()

                // Set status
                tvStatus.text = circle.getStatusText()
                try {
                    tvStatus.setTextColor(Color.parseColor(circle.getStatusColor()))
                    cardCircle.setCardBackgroundColor(Color.parseColor(circle.getBackgroundColor()))
                } catch (e: Exception) {
                    // Fallback colors
                }

                // Handle member count display
                val memberCount = circle.memberCount
                when {
                    memberCount <= 3 -> {
                        // Hide member count badge
                        ivMemberCount.visibility = View.GONE
                        tvMemberCount.visibility = View.GONE
                    }

                    else -> {
                        // Show member count badge
                        ivMemberCount.visibility = View.VISIBLE
                        tvMemberCount.visibility = View.VISIBLE
                        tvMemberCount.text = circle.getMemberCountText()
                    }
                }

                // Configure join button based on status
                when (circle.status) {
                    "RECRUITING" -> {
                        btnJoin.text = "Join Etibe"
                        btnJoin.isEnabled = !circle.isFull
                        btnJoin.alpha = if (circle.isFull) 0.5f else 1f
                        // Set green background for recruiting
                        btnJoin.setBackgroundResource(com.etibe.app.R.drawable.bg_btn)
                    }

                    "ACTIVE" -> {
                        btnJoin.text = "Join Etibe"
                        btnJoin.isEnabled = !circle.isFull
                        btnJoin.alpha = if (circle.isFull) 0.5f else 1f
                        // Set orange/yellow background for active
                        btnJoin.setBackgroundResource(com.etibe.app.R.drawable.bg_button_join_orange)
                    }

                    else -> {
                        btnJoin.text = "View Details"
                        btnJoin.isEnabled = true
                        btnJoin.alpha = 1f
                        btnJoin.setBackgroundResource(com.etibe.app.R.drawable.bg_btn)
                    }
                }

                // Click listeners
                root.setOnClickListener { onCircleClick(circle) }
                btnJoin.setOnClickListener { onJoinClick(circle) }
            }
        }
    }

    class CircleDiffCallback : DiffUtil.ItemCallback<Circle>() {
        override fun areItemsTheSame(oldItem: Circle, newItem: Circle): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Circle, newItem: Circle): Boolean {
            return oldItem == newItem
        }
    }
}