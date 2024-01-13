package com.fajar.myemarket.utils

import androidx.fragment.app.Fragment
import com.fajar.myemarket.ui.buyer.MainActivity
import com.fajar.myemarket.ui.seller.SellerActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Fragment.hideBottomNavigationView(){
    val bottomNavigationView =
        (activity as MainActivity).findViewById<BottomNavigationView>(
            com.fajar.myemarket.R.id.bottom_nav
        )
    bottomNavigationView.visibility = android.view.View.GONE
}

fun Fragment.showBottomNavigationView(){
    val bottomNavigationView =
        (activity as MainActivity).findViewById<BottomNavigationView>(
            com.fajar.myemarket.R.id.bottom_nav
        )
    bottomNavigationView.visibility = android.view.View.VISIBLE
}

fun Fragment.hideBottomNavView(){
    val bottomNavigationView =
        (activity as SellerActivity).findViewById<BottomNavigationView>(
            com.fajar.myemarket.R.id.bottom_nav
        )
    bottomNavigationView.visibility = android.view.View.VISIBLE
}

fun Fragment.showBottomNavView(){
    val bottomNavigationView =
        (activity as SellerActivity).findViewById<BottomNavigationView>(
            com.fajar.myemarket.R.id.bottom_nav
        )
    bottomNavigationView.visibility = android.view.View.VISIBLE
}

