package com.fajar.myemarket.ui.seller.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fajar.myemarket.R
import com.fajar.myemarket.core.adapter.SellerProductAdapter
import com.fajar.myemarket.databinding.FragmentSellerItemBinding
import com.fajar.myemarket.utils.Resource
import com.fajar.myemarket.utils.VerticalItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SellerItemFragment: Fragment(R.layout.fragment_seller_item) {

    private lateinit var binding: FragmentSellerItemBinding
    private val productAdapter by lazy { SellerProductAdapter() }
    private val viewModel by activityViewModels<SellerItemViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSellerItemBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProductRV()

        lifecycleScope.launchWhenStarted {
            viewModel.itemSeller.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarCart.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressbarCart.visibility = View.INVISIBLE
                        if (it.data!!.isEmpty()) {
                            showEmptyCart()
                            hideOtherViews()
                        } else {
                            hideEmptyCart()
                            showOtherViews()
                            productAdapter.differ.submitList(it.data)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressbarCart.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        productAdapter.onProductClick = {
            val action =
                SellerItemFragmentDirections.actionSellerOrderFragmentToItemDetailFragment(it)
            findNavController().navigate(action)
        }

        binding.fab.setOnClickListener {
            val action =
                SellerItemFragmentDirections.actionSellerItemFragmentToUploadFragment()
            findNavController().navigate(action)
        }


    }

    private fun setupProductRV(){
        binding.rvItem.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
            adapter = productAdapter
            addItemDecoration(VerticalItemDecoration())
        }
    }

    private fun showOtherViews() {
        binding.apply {
            rvItem.visibility = View.VISIBLE

        }
    }

    private fun hideOtherViews() {
        binding.apply {
            rvItem.visibility = View.GONE

        }
    }

    private fun hideEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility = View.GONE
        }
    }

    private fun showEmptyCart() {
        binding.apply {
            layoutCartEmpty.visibility = View.VISIBLE
        }
    }


}