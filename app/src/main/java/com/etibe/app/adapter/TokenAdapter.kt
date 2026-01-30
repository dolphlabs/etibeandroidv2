package com.etibe.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.etibe.app.SelectTokenBottomSheet
import com.etibe.app.databinding.ItemTokenBinding

class TokenAdapter(
    private val tokens: List<SelectTokenBottomSheet.Token>,
    private val currentlySelected: String,
    private val onTokenClick: (SelectTokenBottomSheet.Token) -> Unit
) : RecyclerView.Adapter<TokenAdapter.TokenViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenViewHolder {
        val binding = ItemTokenBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TokenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TokenViewHolder, position: Int) {
        val token = tokens[position]
        holder.bind(token, token.symbol == currentlySelected)
    }

    override fun getItemCount(): Int = tokens.size

    inner class TokenViewHolder(private val binding: ItemTokenBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(token: SelectTokenBottomSheet.Token, isSelected: Boolean) {
            binding.apply {
                tvToken.text = token.symbol
                ivTokenIcon.setImageResource(token.iconResId)
                ivCheck.isVisible = isSelected

                root.setOnClickListener {
                    onTokenClick(token)
                }
            }
        }
    }
}