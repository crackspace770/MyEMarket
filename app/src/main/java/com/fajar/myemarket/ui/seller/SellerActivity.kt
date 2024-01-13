package com.fajar.myemarket.ui.seller


import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fajar.myemarket.R
import com.fajar.myemarket.databinding.ActivityMainBinding
import com.fajar.myemarket.databinding.ActivitySellerBinding
import com.fajar.myemarket.ui.buyer.cart.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerActivity :AppCompatActivity(){

    val binding by lazy {
        ActivitySellerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_seller_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomSellerNav.setupWithNavController(navController)


    }

}