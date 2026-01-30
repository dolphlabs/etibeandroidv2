package com.etibe.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.etibe.app.adapter.TokenAdapter
import com.etibe.app.databinding.BottomSheetSelectTokenBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectTokenBottomSheet(
    private val selectedToken: String,
    private val onTokenSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSelectTokenBinding? = null
    private val binding get() = _binding!!

    private val tokens = listOf(
        Token("USDC", R.drawable.ic_usdc_logo),  // ← replace with your real icon resources
        Token("USDT", R.drawable.ic_usdt_logo),
        Token("NEAR", R.drawable.ic_near_logo)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSelectTokenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupRecyclerView()
        setupCloseButton()
    }

    private fun setupRecyclerView() {
        binding.rvTokens.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TokenAdapter(
                tokens = tokens,
                currentlySelected = selectedToken,
                onTokenClick = { token ->
                    onTokenSelected(token.symbol)
                    dismiss()
                }
            )
            setHasFixedSize(true)
        }
    }

    private fun setupCloseButton() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class Token(
        val symbol: String,
        val iconResId: Int
    )
}