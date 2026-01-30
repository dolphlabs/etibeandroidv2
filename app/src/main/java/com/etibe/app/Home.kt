package com.etibe.app

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.etibe.app.databinding.FragmentHomeBinding
import com.etibe.app.models.RetrofitClient
import com.etibe.app.utils.UserResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recentActivityAdapter: RecentActivityAdapter
    private lateinit var myEtibeAdapter: MyEtibeAdapter

    private var isBalanceVisible = false

    private var selectedToken: String = "Near"          // default shown token

    private var balancesMap: Map<String, Double> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupClickListeners()
        loadUserProfileAndBalance()  // main data load
        loadMockData()
        setupCurrencyDropdown()          // ← new// temporary mock until real endpoints exist
    }

    private fun setupCurrencyDropdown() {
        binding.currencySelector.setOnClickListener {
            SelectTokenBottomSheet(
                selectedToken = selectedToken,
                onTokenSelected = { newToken ->
                    selectedToken = newToken
                    binding.tvCurrency.text = newToken
                    updateBalanceDisplay()
                }
            ).show(parentFragmentManager, "SelectTokenBottomSheet")
        }
    }

    private fun setupRecyclerViews() {
        recentActivityAdapter = RecentActivityAdapter { activity ->
            onActivityClicked(activity)
        }
        binding.rvRecentActivity.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentActivityAdapter
            setHasFixedSize(true)
        }

        myEtibeAdapter = MyEtibeAdapter { etibe ->
            onEtibeClicked(etibe)
        }
        binding.rvMyEtibe.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myEtibeAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadUserProfileAndBalance() = lifecycleScope.launch {
        showLoading(true)

        try {
            val response = RetrofitClient.instance(requireContext()).getCurrentUser()

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data?.user != null) {
                    updateUIWithUser(body.data.user)
                } else {
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (response.code() == 401) {
                    Toast.makeText(context, "Session expired", Toast.LENGTH_SHORT).show()
                    goToLogin()
                } else {
                    Toast.makeText(context, "Error ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            showLoading(false)
        }
    }

    private fun updateUIWithUser(user: com.etibe.app.utils.User) {
        val wallet = user.walletBalance ?: return

        // Parse real balances from API (they are Strings)
        val near = wallet.NEAR?.toDoubleOrNull() ?: 0.0
        val usdt = wallet.USDT?.toDoubleOrNull() ?: 0.0
        val usdc = wallet.USDC?.toDoubleOrNull() ?: 0.0

        // Store in map for easy access when switching token
        balancesMap = mapOf(
            "NEAR" to near,
            "USDT" to usdt,
            "USDC" to usdc
        )

        // Show current selected token balance
        updateBalanceDisplay()
    }

    private fun updateBalanceDisplay() {
        val amount = balancesMap[selectedToken] ?: 0.0

        val displayText: String
        val approxText: String

        when (selectedToken) {
            "NEAR" -> {
                displayText = if (isBalanceVisible) String.format("%.5f NEAR", amount) else "****"
                approxText = if (isBalanceVisible) {
                    // Rough conversion – replace with real rate API later
                    val nearUsdRate = 4.80
                    "~ $${String.format("%.2f", amount * nearUsdRate)}"
                } else "****"
            }

            else -> { // USDC & USDT
                displayText = if (isBalanceVisible) String.format("$%.2f", amount) else "****"
                approxText = if (isBalanceVisible) "~ $${String.format("%.2f", amount)}" else "****"
            }
        }

        binding.apply {
            tvBalance.text = displayText
            tvApprox.text = approxText
            tvCurrency.text = selectedToken
        }
    }

    private fun updateBalance(mainAmount: Double, approxUsd: Double) {
        binding.apply {
            tvBalance.text =
                if (isBalanceVisible) "$${String.format("%.2f", mainAmount)}" else "****"
            tvApprox.text =
                if (isBalanceVisible) "~ $${String.format("%.2f", approxUsd)}" else "****"
        }
    }

    private fun loadMockData() {

    }

    private fun showLoading(show: Boolean) {

        binding.progressBar?.indeterminateDrawable?.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.primary_green),
            PorterDuff.Mode.SRC_IN
        )
    }

    private fun goToLogin() {
        // Clear session / tokens
        RetrofitClient.clearTokens(requireContext())


        // Option 2: Navigate via nav graph (if using single activity)
        // findNavController().navigate(R.id.action_home_to_login)
    }

    private fun toggleBalanceVisibility() {
        isBalanceVisible = !isBalanceVisible
        // Re-apply current balance (lazy way)
        // You could store current values in variables instead
        loadUserProfileAndBalance() // simplest but not optimal - reloads data
        // Better: store balances in fragment variables and just toggle visibility
    }

    // ─────────────────────────────────────────────
    // Click listeners (most are placeholders)
    // ─────────────────────────────────────────────

    private fun setupClickListeners() {
        binding.apply {
            btnTopUp.setOnClickListener { showTopUpDialog() }
            btnWithdraw.setOnClickListener { handleWithdraw() }
            ivEyeIcon.setOnClickListener { toggleBalanceVisibility() }

            btnCreateEtibe.setOnClickListener { navigateToCreateEtibe() }
            btnJoinEtibe.setOnClickListener { navigateToJoinEtibe() }
            btnExploreEtibe.setOnClickListener { navigateToExploreEtibe() }
            btnMyEtibe.setOnClickListener { navigateToMyEtibe() }

            tvViewAllActivity.setOnClickListener { navigateToAllActivities() }
            tvViewAllEtibe.setOnClickListener { navigateToAllEtibe() }

            ivNotification.setOnClickListener { navigateToNotifications() }
            ivProfile.setOnClickListener { navigateToProfile() }

            // currencySelector / tvCurrency → implement later when needed
        }
    }

    private fun showTopUpDialog() {
        TopUpBottomSheetDialog()
            .apply {
                setOnTopUpSuccessListener { amount ->
                    // Refresh balance + add fake activity for now
                    loadUserProfileAndBalance()
                    // addNewActivity(...) // ← implement if needed
                }
            }
            .show(childFragmentManager, "TopUpDialog")
    }

    // Placeholder navigation methods (uncomment/adjust when routes exist)
    private fun navigateToCreateEtibe() { /* findNavController().navigate(...) */
    }

    private fun navigateToJoinEtibe() { /* ... */
    }

    private fun navigateToExploreEtibe() { /* ... */
    }

    private fun navigateToMyEtibe() { /* ... */
    }

    private fun navigateToAllActivities() { /* ... */
    }

    private fun navigateToAllEtibe() { /* ... */
    }

    private fun navigateToNotifications() { /* ... */
    }

    private fun navigateToProfile() { /* ... */
    }

    private fun handleWithdraw() {
        // TODO: Implement withdraw flow
        Toast.makeText(context, "Withdraw feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun onActivityClicked(activity: RecentActivity) { /* TODO */
    }

    private fun onEtibeClicked(etibe: MyEtibe) { /* TODO */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}