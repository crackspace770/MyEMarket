package com.fajar.myemarket.ui.buyer.billing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fajar.myemarket.core.model.Order
import com.fajar.myemarket.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BillingOrderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel() {

    private val _order = MutableStateFlow<Resource<Order>>(Resource.Unspecified())
    val order = _order.asStateFlow()

    fun placeOrder(order: Order) {
        viewModelScope.launch {
            _order.emit(Resource.Loading())
        }

        val userId = auth.uid

        if (userId != null) {
            // Retrieve the buyer's name from the user document
            firestore.collection("user").document(userId).get()
                .addOnSuccessListener { userDocument ->
                    val buyerName = userDocument.getString("name")

                    if (buyerName != null) {
                        // Include the buyer's name in the order data
                        order.buyerName = buyerName

                        // Place the order in the user's "orders" collection
                        order.products.forEach { cartProduct ->
                            val productId = cartProduct.product.id

                            // Create a new order for each product from the cart
                            val productOrder = Order(
                                orderStatus = order.orderStatus,
                                totalPrice = cartProduct.product.price,
                                products = listOf(cartProduct),
                                address = order.address,
                                date = order.date,
                                orderId = order.orderId
                            )

                            // Place the order in the user's "orders" collection
                            firestore.collection("user")
                                .document(userId)
                                .collection("orders")
                                .document(productId)
                                .set(productOrder)

                            firestore.collection("orders").document().set(order)

                        }

                        // Remove products from the user's "cart" collection
                        firestore.collection("user").document(userId).collection("cart").get()
                            .addOnSuccessListener {
                                it.documents.forEach {
                                    it.reference.delete()
                                }
                            }

                        viewModelScope.launch {
                            _order.emit(Resource.Success(order))
                        }
                    } else {
                        viewModelScope.launch {
                            _order.emit(Resource.Error("Buyer's name is null"))
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    viewModelScope.launch {
                        _order.emit(Resource.Error("Failed to retrieve buyer's name: ${exception.message}"))
                    }
                }
        } else {
            viewModelScope.launch {
                _order.emit(Resource.Error("User ID is null"))
            }
        }
    }
}
