package com.etibe.app

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.etibe.app.databinding.FragmentOnboardingPageBinding


class OnboardingPageFragment : Fragment() {
    private var _binding: FragmentOnboardingPageBinding? = null
    private val binding get() = _binding!!

    private val title: String by lazy { arguments?.getString(ARG_TITLE) ?: "" }
    private val description: String by lazy { arguments?.getString(ARG_DESCRIPTION) ?: "" }
    private val highlightWords: Array<String> by lazy {
        arguments?.getStringArray(ARG_HIGHLIGHT_WORDS) ?: emptyArray()
    }
    private val highlightColorRes: Int by lazy {
        arguments?.getInt(ARG_HIGHLIGHT_COLOR, R.color.highlight_yellow) ?: R.color.highlight_yellow
    }
    private val imageRes: Int by lazy { arguments?.getInt(ARG_IMAGE_RES) ?: 0 }
    private val isLastPage: Boolean by lazy { arguments?.getBoolean(ARG_IS_LAST_PAGE) ?: false }
    private val showBackButton: Boolean by lazy { arguments?.getBoolean(ARG_SHOW_BACK) ?: false }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOnboardingPageBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        binding.apply {
            tvTitle.text = title

            // Apply highlighted text if there are words to highlight
            if (description.isNotEmpty()) {
                tvDescription.text = if (highlightWords.isNotEmpty()) {
                    getHighlightedText(description, highlightWords)
                } else {
                    description
                }
            }

            ivOnboarding.setImageResource(imageRes)

            // Show/hide elements based on page
            if (isLastPage) {
                btnNext.visibility = View.GONE
                groupAuthButtons.visibility = View.VISIBLE
                tvDescription.visibility = View.GONE
            } else {
                btnNext.visibility = View.VISIBLE
                groupAuthButtons.visibility = View.GONE
                tvDescription.visibility = if (description.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun getHighlightedText(text: String, wordsToHighlight: Array<String>): SpannableString {
        val spannable = SpannableString(text)
        val backgroundColor = ContextCompat.getColor(requireContext(), highlightColorRes)

        wordsToHighlight.forEach { word ->
            var startIndex = 0
            while (startIndex < text.length) {
                val index = text.indexOf(word, startIndex, ignoreCase = true)
                if (index == -1) break

                // Apply background highlight with custom color
                spannable.setSpan(
                    BackgroundColorSpan(backgroundColor),
                    index,
                    index + word.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                startIndex = index + word.length
            }
        }

        return spannable
    }

    private fun setupClickListeners() {
        binding.apply {
            btnNext.setOnClickListener {
                // Navigate to next page
                val viewPager = parentFragment?.view?.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)
                viewPager?.currentItem = (viewPager.currentItem + 1)
            }

            btnLogin.setOnClickListener {
                // Navigate to login screen
            }

            btnRegister.setOnClickListener {
                // Navigate to register screen
            }

            btnGoogleSignIn.setOnClickListener {
                // Handle Google Sign-In
                handleGoogleSignIn()
            }
        }
    }

    private fun handleGoogleSignIn() {
        // Implement Google Sign-In logic
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_HIGHLIGHT_WORDS = "highlight_words"
        private const val ARG_HIGHLIGHT_COLOR = "highlight_color"
        private const val ARG_IMAGE_RES = "image_res"
        private const val ARG_IS_LAST_PAGE = "is_last_page"
        private const val ARG_SHOW_BACK = "show_back"

        fun newInstance(
            title: String,
            description: String,
            imageRes: Int,
            highlightWords: Array<String> = emptyArray(),
            highlightColor: Int = R.color.highlight_yellow,
            isLastPage: Boolean = false,
            showBackButton: Boolean = false
        ): OnboardingPageFragment {
            return OnboardingPageFragment().apply {
                arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_DESCRIPTION to description,
                    ARG_HIGHLIGHT_WORDS to highlightWords,
                    ARG_HIGHLIGHT_COLOR to highlightColor,
                    ARG_IMAGE_RES to imageRes,
                    ARG_IS_LAST_PAGE to isLastPage,
                    ARG_SHOW_BACK to showBackButton
                )
            }
        }
    }
}