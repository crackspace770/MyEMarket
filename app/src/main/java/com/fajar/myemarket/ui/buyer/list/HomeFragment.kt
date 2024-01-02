package com.fajar.myemarket.ui.buyer.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fajar.myemarket.adapter.HomeViewPagerAdapter
import com.fajar.myemarket.databinding.FragmentHomeBinding
import com.fajar.myemarket.ui.buyer.category.AccessoriesFragment
import com.fajar.myemarket.ui.buyer.category.ChairFragment
import com.fajar.myemarket.ui.buyer.category.CupboardFragment
import com.fajar.myemarket.ui.buyer.category.FurnitureFragment
import com.fajar.myemarket.ui.buyer.category.MainCategoryFragment
import com.fajar.myemarket.ui.buyer.category.TableFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment:Fragment() {

    private var binding: FragmentHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesFragment = arrayListOf<Fragment>(
            MainCategoryFragment(),
            ChairFragment(),
            TableFragment(),
            CupboardFragment(),
            AccessoriesFragment(),
            FurnitureFragment()
        )

        binding?.viewpagerHome?.isUserInputEnabled = false

        val viewPagerAdapter = HomeViewPagerAdapter(categoriesFragment,childFragmentManager,lifecycle)
        binding?.viewpagerHome?.adapter = viewPagerAdapter
        binding?.let {
            TabLayoutMediator(it.tabLayout, binding!!.viewpagerHome) { tab, position ->
                when (position) {
                    0 -> tab.text = "Main"
                    1 -> tab.text = "Chair"
                    2 -> tab.text = "Cupboard"
                    3 -> tab.text = "Table"
                    4 -> tab.text = "Accessories"
                    5 -> tab.text = "Furniture"

                }
            }.attach()
        }

    }

}