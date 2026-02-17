package com.etibe.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.etibe.app.R
import com.etibe.app.withdraw.WithdrawFundsFragment
import com.etibe.app.databinding.ItemCryptoOptionBinding

class CryptoOptionAdapter(
    private val options: List<WithdrawFundsFragment.CryptoOption>,
    private val onSelect: (WithdrawFundsFragment.CryptoOption) -> Unit
) : RecyclerView.Adapter<CryptoOptionAdapter.ViewHolder>() {

    private var selectedPosition = options.indexOfFirst { it.isSelected }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCryptoOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.bind(option, position == selectedPosition)
    }

    override fun getItemCount() = options.size

    inner class ViewHolder(private val binding: ItemCryptoOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(option: WithdrawFundsFragment.CryptoOption, isSelected: Boolean) {
            binding.apply {
                tvSymbol.text = option.symbol
                tvBalance.text = String.format("%.2f", option.balance)

                // Icon placeholder (replace with real icons later)
                ivIcon.setImageResource(
                    when (option.symbol) {
                        "USDC" -> R.drawable.ic_usdc_logo
                        "USDT" -> R.drawable.ic_usdt_logo
                        "NEAR" -> R.drawable.ic_near_logo
                        else -> R.drawable.ic_near_logo
                    }
                )

                root.setBackgroundResource(
                    if (isSelected) R.drawable.bg_crypto_selected
                    else R.drawable.bg_crypto_unselected
                )

                root.setOnClickListener {
                    val previous = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(previous)
                    notifyItemChanged(selectedPosition)
                    onSelect(option)
                }
            }
        }
    }
}