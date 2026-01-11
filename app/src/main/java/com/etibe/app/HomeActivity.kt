package com.etibe.app

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        val navController = findNavController(R.id.fragment)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Link bottom navigation with navigation graph
        bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
   /*         when (destination.id) {
                R.id.checkoutFragment,
                R.id.fragment_paystack_webview,
                R.id.fragment_gift_form,
                R.id.fragment_order_note,
                R.id.editAddress,
                R.id.editProfile,
                R.id.wallet,
                    -> {
                    bottomNavigationView.visibility = View.GONE
                }

                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }*/
        }
    }
}