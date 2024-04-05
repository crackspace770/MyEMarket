package com.fajar.myemarket.ui.buyer.billing

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.RecyclerView
import com.fajar.myemarket.R
import com.fajar.myemarket.core.adapter.AddressAdapter
import com.fajar.myemarket.core.adapter.BillingProductAdapter
import com.fajar.myemarket.core.model.Address
import com.fajar.myemarket.core.model.CartProduct
import com.fajar.myemarket.core.model.Order
import com.fajar.myemarket.core.model.OrderStatus
import com.fajar.myemarket.databinding.FragmentBillingBinding
import com.fajar.myemarket.ui.buyer.profile.ProfileFragment
import com.fajar.myemarket.utils.Constants.BASE_URL_MIDTRANS
import com.fajar.myemarket.utils.Constants.CLIENT_KEY_MIDTRANS
import com.fajar.myemarket.utils.HorizontalItemDecoration
import com.fajar.myemarket.utils.Resource
import com.fajar.myemarket.utils.loadImageUrl
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme
import com.midtrans.sdk.corekit.models.BillingAddress
import com.midtrans.sdk.corekit.models.CustomerDetails
import com.midtrans.sdk.corekit.models.ShippingAddress
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class BillingFragment:Fragment() {

    private lateinit var binding: FragmentBillingBinding
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductsAdapter by lazy { BillingProductAdapter() }
    private val billingViewModel by viewModels<BillingViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f
    private lateinit var auth: FirebaseAuth
    private var db: FirebaseFirestore? = null
    private var selectedAddress: Address? = null
    private val billingOrderViewModel by viewModels<BillingOrderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        products = args.products.toList()
        totalPrice = args.totalPrice
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillingBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }

        setupBillingProductsRv()
        setupAddressRv()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (!args.payment) {
            binding.apply {
                buttonPlaceOrder.visibility = View.INVISIBLE
                totalBoxContainer.visibility = View.INVISIBLE
                middleLine.visibility = View.INVISIBLE
                bottomLine.visibility = View.INVISIBLE
            }
        }

        lifecycleScope.launchWhenStarted {
            billingViewModel.address.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        addressAdapter.differ.submitList(it.data)
                        binding.progressbarAddress.visibility = View.GONE
                    }

                    is Resource.Error -> {
                        binding.progressbarAddress.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            billingOrderViewModel.order.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                   //     binding.buttonPlaceOrder.startAnimation()
                    }

                    is Resource.Success -> {
                      //  binding.buttonPlaceOrder.revertAnimation()
                        findNavController().navigateUp()
                        Snackbar.make(requireView(), "Your order was placed", Snackbar.LENGTH_LONG)
                            .show()
                    }

                    is Resource.Error -> {
                        //binding.buttonPlaceOrder.revertAnimation()
                        Toast.makeText(requireContext(), "Error ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> Unit
                }
            }
        }

        billingProductsAdapter.differ.submitList(products)

        binding.tvTotalPrice.text = "$ $totalPrice"

        addressAdapter.onClick = {
            selectedAddress = it
            if (!args.payment) {
                val b = Bundle().apply { putParcelable("address", selectedAddress) }
                findNavController().navigate(R.id.action_billingFragment_to_addressFragment, b)
            }

        }

        binding.buttonPlaceOrder.setOnClickListener {
            if(selectedAddress == null) {
                Toast.makeText(requireContext(), "Please select your address", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            showOrderConfirmationDialogue()
        }

    }


    private fun showOrderConfirmationDialogue(){
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Order items")
            setMessage("Do you want to order your items?")
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Yes") { dialog, _ ->
                val order = Order(
                    OrderStatus.Ordered.status,
                    totalPrice,
                    products,
                    selectedAddress!!
                )
                billingOrderViewModel.placeOrder(order)

                redirectToMidtrans()

                dialog.dismiss()
            }
        }
        alertDialog.create()
        alertDialog.show()
    }

    private fun redirectToMidtrans() {
        val itemDetails = ArrayList<com.midtrans.sdk.corekit.models.ItemDetails>()

        val userId = auth.currentUser!!.uid
        val dataUser = db?.collection("user")?.document(userId)

        // Construct item details with order information and calculate total amount
        var totalAmount = 0.0
        for (cartProduct in products) {
            val product = cartProduct.product
            val detail = com.midtrans.sdk.corekit.models.ItemDetails(
                product.id,
                product.price.toDouble(),
                cartProduct.quantity,
                product.name
            )
            itemDetails.add(detail)
            totalAmount += product.price.toDouble() * cartProduct.quantity
        }

        // Create TransactionRequest with total amount
        val transactionRequest = TransactionRequest(
            "Payment-Midtrans" + System.currentTimeMillis().toString(),
            totalAmount
        )

        // Set item details
        transactionRequest.itemDetails = itemDetails

        dataUser?.addSnapshotListener { snapshot, exception ->
            exception?.let {
                Log.d(TAG, it.message.toString())
                return@addSnapshotListener
            }
            snapshot?.let {
                val name = it.get("fullName")
                val email = it.get("email")
                val phone = it.get("nomorHp")
                val alamat = it.get("alamat")

                // Set customer details
                val customerDetails = CustomerDetails()
                customerDetails.customerIdentifier = name.toString()
                customerDetails.phone = phone.toString()
                customerDetails.email = email.toString()
                val shippingAddress = ShippingAddress()
                shippingAddress.address = alamat.toString()
                val billingAddress = BillingAddress()
                billingAddress.address = alamat.toString()

                customerDetails.billingAddress = billingAddress
                transactionRequest.customerDetails = customerDetails

                // Set other Midtrans SDK configurations
                SdkUIFlowBuilder.init()
                    .setClientKey(CLIENT_KEY_MIDTRANS)
                    .setContext(requireContext())
                    .setTransactionFinishedCallback { result ->
                        // Handle transaction result here
                        // You can use result.status to determine the transaction status
                    }
                    .setMerchantBaseUrl(BASE_URL_MIDTRANS)
                    .enableLog(true)
                    .setColorTheme(CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
                    .setLanguage("id")
                    .buildSDK()

                // Start payment UI flow
                MidtransSDK.getInstance().transactionRequest = transactionRequest
                MidtransSDK.getInstance().startPaymentUiFlow(requireContext())
            }
        }
    }


    private fun setupAddressRv() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    private fun setupBillingProductsRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductsAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    companion object {
        const val TAG = "BillingFragment"
    }


}

