package com.fajar.myemarket.ui.buyer.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fajar.myemarket.R
import com.fajar.myemarket.core.adapter.ColorAdapter
import com.fajar.myemarket.core.adapter.SizeAdapter
import com.fajar.myemarket.core.adapter.ViewPager2Images
import com.fajar.myemarket.core.model.CartProduct
import com.fajar.myemarket.databinding.FragmentDetailBinding
import com.fajar.myemarket.utils.Resource
import com.fajar.myemarket.utils.hideBottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProductDetailFragment: Fragment() {

    private val args by navArgs<ProductDetailFragmentArgs>()
    private lateinit var binding: FragmentDetailBinding
    private val viewPagerAdapter by lazy { ViewPager2Images() }
    private val sizesAdapter by lazy { SizeAdapter() }
    private val colorsAdapter by lazy { ColorAdapter() }
    private var selectedColor: Int? = null
    private var selectedSize: String? = null
    private val viewModel by viewModels<DetailViewModel>()


  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    hideBottomNavigationView()
    binding = FragmentDetailBinding.inflate(inflater)
    return binding.root
  }

  @SuppressLint("SetTextI18n")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val product = args.product

    setupSizesRv()
    setupColorsRv()
    setupViewpager()

    binding.imageClose.setOnClickListener {
      findNavController().navigateUp()
    }

    sizesAdapter.onItemClick = {
      selectedSize = it
    }

    colorsAdapter.onItemClick = {
      selectedColor = it
    }

    binding.buttonAddToCart.setOnClickListener {
      viewModel.addUpdateProductInCart(CartProduct(product, 1, selectedColor, selectedSize))
    }

    lifecycleScope.launchWhenStarted {
      viewModel.addToCart.collectLatest {
        when (it) {
          is Resource.Loading -> {
          //  binding.buttonAddToCart.startAnimation()
          }

          is Resource.Success -> {

            binding.buttonAddToCart.setBackgroundColor(resources.getColor(R.color.purple2))
          }

          is Resource.Error -> {
         //   binding.buttonAddToCart.stopAnimation()
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
          }
          else -> Unit
        }
      }
    }

    binding.apply {
      tvProductName.text = product.name
      tvProductPrice.text = "$ ${product.price}"
      tvProductDescription.text = product.description
      tvSellerName.text = "By ${product.sellerName}"
      if (product.colors.isNullOrEmpty())
        tvProductColors.visibility = View.INVISIBLE
      if (product.sizes.isNullOrEmpty())
        tvProductSize.visibility = View.INVISIBLE
    }

    viewPagerAdapter.differ.submitList(product.images)
    product.colors?.let { colorsAdapter.differ.submitList(it) }
    product.sizes?.let { sizesAdapter.differ.submitList(it) }

  }

  private fun setupViewpager() {
    binding.apply {
      viewPagerProductImages.adapter = viewPagerAdapter
    }
  }

  private fun setupColorsRv() {
    binding.rvColors.apply {
      adapter = colorsAdapter
      layoutManager =
        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }
  }

  private fun setupSizesRv() {
    binding.rvSizes.apply {
      adapter = sizesAdapter
      layoutManager =
        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }
  }

}