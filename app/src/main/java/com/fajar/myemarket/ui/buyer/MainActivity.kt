package com.fajar.myemarket.ui.buyer

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fajar.myemarket.R
import com.fajar.myemarket.databinding.ActivityMainBinding
import com.fajar.myemarket.ui.buyer.cart.CartFragment
import com.fajar.myemarket.ui.buyer.list.HomeFragment
import com.fajar.myemarket.ui.buyer.profile.ProfileFragment
import com.fajar.myemarket.utils.NetworkConnectionLiveData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkConnection: NetworkConnectionLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        networkConnection = NetworkConnectionLiveData(this)
        networkConnection.observe(this) { isInternetAvailable ->
            if (isInternetAvailable) {
                setContentView(binding.root)
            } else {
                setContentView(R.layout.network_error)
            }
        }

        binding.apply {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            setupBottomNavMenu(navController)
        }
    }

    private fun setupBottomNavMenu(navController: NavController) {
        val bottomNav = binding.navView
        bottomNav.setupWithNavController(navController)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> navigateToFragment(HomeFragment())
                R.id.cartFragment -> navigateToFragment(CartFragment())
                R.id.profileFragment -> navigateToFragment(ProfileFragment())
                else -> navigateToFragment(HomeFragment())
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()

    }
}