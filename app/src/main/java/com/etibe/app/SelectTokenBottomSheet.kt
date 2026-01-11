package com.etibe.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout


class SelectTokenBottomSheet(
    private val selectedToken: String,
    private val onTokenSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private val tokens = listOf("USDC", "USDT", "NEAR")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_select_token, container, false)

        val close = view.findViewById<ImageView>(R.id.ivClose)
        val containerLayout = view.findViewById<LinearLayout>(R.id.tokenContainer)

        close.setOnClickListener { dismiss() }

        containerLayout.removeAllViews()

        tokens.forEach { token ->
            val item = inflater.inflate(R.layout.item_token, containerLayout, false)
            val tvToken = item.findViewById<TextView>(R.id.tvToken)
            val check = item.findViewById<ImageView>(R.id.ivCheck)

            tvToken.text = token
            check.visibility = if (token == selectedToken) View.VISIBLE else View.GONE

            item.setOnClickListener {
                onTokenSelected(token)
                dismiss()
            }

            containerLayout.addView(item)
        }

        return view
    }
}
