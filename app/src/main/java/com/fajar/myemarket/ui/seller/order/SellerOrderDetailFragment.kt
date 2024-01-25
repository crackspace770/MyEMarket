package com.fajar.myemarket.ui.seller.order

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.fajar.myemarket.R
import com.fajar.myemarket.core.model.OrderStatus
import com.fajar.myemarket.core.model.getOrderStatus
import com.fajar.myemarket.databinding.FragmentSellerOrderDetailBinding
import com.fajar.myemarket.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SellerOrderDetailFragment:Fragment(R.layout.fragment_seller_order_detail) {

    private lateinit var binding: FragmentSellerOrderDetailBinding
    private val args by navArgs<SellerOrderDetailFragmentArgs>()
    private val viewModel by viewModels<SellerOrderDetailViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSellerOrderDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val order = args.order

        binding.apply {

            tvOrderId.text = "Order #${order.orderId}"


            stepView.setSteps(
                mutableListOf(
                    OrderStatus.Ordered.status,
                    OrderStatus.Confirmed.status,
                    OrderStatus.Shipped.status,
                    OrderStatus.Delivered.status,
                )
            )

            val currentOrderState = when (getOrderStatus(order.orderStatus)) {
                is OrderStatus.Ordered -> 0
                is OrderStatus.Confirmed -> 1
                is OrderStatus.Shipped -> 2
                is OrderStatus.Delivered -> 3
                else -> 0
            }

            stepView.go(currentOrderState, false)
            if (currentOrderState == 3) {
                stepView.done(true)
            }

            //Address
            tvFullName.text = order.address.fullName
            tvAddress.text = "${order.address.street} ${order.address.city}"
            tvPhoneNumber.text = order.address.phone

            //Product Ordered
            tvTotalPrices.text = "$ ${order.totalPrice}"
            tvProductsName.text = order.products.firstOrNull()?.product?.name
            tvPrices.text = "$ ${order.products.firstOrNull()?.product?.price.toString()}"
            tvQuantities.text = order.products.firstOrNull()?.quantity.toString()

            Glide.with(requireContext())
                .load(order.products.firstOrNull()?.product?.images?.get(0))
                .into(imgProduct)

            btnConfirm.setOnClickListener {
                lifecycleScope.launch {
                    viewModel.updateOrderStatus(order.orderId, "Confirmed")
                }
            }

            btnCancel.setOnClickListener {
                lifecycleScope.launch {
                    viewModel.updateOrderStatus(order.orderId, "Canceled")
                }
            }

        }

        lifecycleScope.launchWhenStarted {
            viewModel.updateOrderStatusResult.collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Handle loading state if needed
                    }
                    is Resource.Success -> {
                        // Order status updated successfully
                        // You may navigate back or perform any other action
                    }
                    is Resource.Error -> {
                        // Failed to update order status
                        Toast.makeText(
                            requireContext(),
                            result.message ?: "Failed to update order status",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> Unit
                }
            }
        }


    }

}