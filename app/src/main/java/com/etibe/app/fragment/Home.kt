package com.etibe.app.fragment

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
import com.etibe.app.utils.MyEtibe
import com.etibe.app.adapter.HomeAdpter
import com.etibe.app.R
import com.etibe.app.utils.RecentActivity
import com.etibe.app.adapter.RecentActivityAdapter
import com.etibe.app.ui.SelectTokenBottomSheet
import com.etibe.app.ui.TopUpBottomSheetDialog
import com.etibe.app.databinding.FragmentHomeBinding
import com.etibe.app.models.RetrofitClient
import com.etibe.app.utils.User
import kotlinx.coroutines.launch

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recentActivityAdapter: RecentActivityAdapter
    private lateinit var myEtibeAdapter: HomeAdpter

    private var isBalanceVisible = false

    private var selectedToken: String = "NEAR"

    private var balancesMap: Map<String, Double> = emptyMap()

    private var userNearAccountId: String? = null

    private val tokenLogos = mapOf(
        "NEAR" to R.drawable.ic_near_logo,
        "USDC" to R.drawable.ic_usdc_logo,
        "USDT" to R.drawable.ic_usdt_logo
        // Add more tokens if needed
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateBalanceDisplay()
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

        myEtibeAdapter = HomeAdpter { etibe ->
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

    private fun updateUIWithUser(user: User) {
        val wallet = user.walletBalance ?: return

        userNearAccountId = user.nearAccountId
        // Parse real balances from API (they are Strings)
        val near = wallet.NEAR?.toDoubleOrNull() ?: 0.0
        val usdt = wallet.USDT?.toDoubleOrNull() ?: 0.0
        val usdc = wallet.USDC?.toDoubleOrNull() ?: 0.0

        balancesMap = mapOf(
            "NEAR" to near,
            "USDT" to usdt,
            "USDC" to usdc
        )

        updateBalanceDisplay()
    }

    private fun updateBalanceDisplay() {
        val amount = balancesMap[selectedToken] ?: 0.0

        val displayText: String
        val approxText: String

        when (selectedToken.uppercase()) {
            "NEAR" -> {
                displayText = if (isBalanceVisible) String.format("%.5f ", amount) else "****"
                approxText = if (isBalanceVisible) {
                    val nearUsdRate = 4.80
                    "~ ${String.format("%.2f", amount * nearUsdRate)}"
                } else "****"
            }

            else -> {
                displayText = if (isBalanceVisible) String.format("%.2f", amount) else "****"
                approxText = if (isBalanceVisible) "~ ${String.format("%.2f", amount)}" else "****"
            }
        }

        binding.apply {
            tvBalance.text = displayText
            tvApprox.text = approxText
            tvCurrency.text = selectedToken

            // ★★★ Update token logo ★★★
            val logoRes = tokenLogos[selectedToken.uppercase()] ?: R.drawable.ic_near_logo
            ivTokenLogo.setImageResource(logoRes)
        }
    }



    private fun loadMockData() {

    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay?.visibility = if (show) View.VISIBLE else View.GONE

        if (show) {
            binding.progressBar?.indeterminateDrawable?.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primary_green),
                PorterDuff.Mode.SRC_IN
            )
        }
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

            // currencySelector / tvCurrency → implement later when needed
        }
    }

    private fun showTopUpDialog() {
        TopUpBottomSheetDialog.Companion.newInstance(userNearAccountId)
            .show(childFragmentManager, TopUpBottomSheetDialog.Companion.TAG)
    }

    // Placeholder navigation methods (uncomment/adjust when routes exist)
    private fun navigateToCreateEtibe() {
    findNavController().navigate(R.id.action_home2_to_fragment_create_etibe)
    }

    private fun navigateToJoinEtibe() {
        findNavController().navigate(R.id.action_home2_to_joinEtibe)

    }

    private fun navigateToExploreEtibe() { /* ... */
    }

    private fun navigateToMyEtibe() {
        findNavController().navigate(R.id.action_home2_to_myEtibeFragment)
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