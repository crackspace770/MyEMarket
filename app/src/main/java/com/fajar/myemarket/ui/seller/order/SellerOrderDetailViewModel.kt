package com.fajar.myemarket.ui.seller.order

import android.util.Log
import androidx.lifecycle.ViewModel
import com.fajar.myemarket.core.firebase.FirebaseCommon
import com.fajar.myemarket.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class SellerOrderDetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
): ViewModel() {

    private val _updateOrderStatusResult = MutableStateFlow<Resource<Unit>>(Resource.Unspecified())
    val updateOrderStatusResult = _updateOrderStatusResult.asStateFlow()


    fun updateOrderStatus(orderId: Long, newStatus: String) {
        val userId = auth.uid

        if (userId != null) {
            // Check if the user is a seller
            firestore.collection("user")
                .document(userId)
                .get()
                .addOnSuccessListener { userDocument ->
                    val isSeller = userDocument.getBoolean("isSeller") ?: false

                    if (isSeller) {
                        // User is a seller, proceed with updating order status
                        Log.d("SellerOrderDetail", "User is a seller, updating order status")

                        // Update the order status in the global "orders" collection
                        updateOrderStatusInCollection("orders", orderId, newStatus)

                        // Update the order status in the user's "orders" collection
                        updateOrderStatusInUserCollection(userId, orderId, newStatus)
                    } else {
                        Log.e("SellerOrderDetail", "User is not a seller")
                        // Handle the case where the user is not a seller
                        _updateOrderStatusResult.value = Resource.Error("Only sellers can update order status.")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("SellerOrderDetail", "Error checking user role: $exception")
                    _updateOrderStatusResult.value = Resource.Error("Failed to check user role.")
                }
        } else {
            Log.e("SellerOrderDetail", "User ID is null")
            _updateOrderStatusResult.value = Resource.Error("User ID is null.")
        }
    }

    private fun updateOrderStatusInCollection(collection: String, orderId: Long, newStatus: String) {
        // Update the order status in the specified collection
        firestore.collection(collection)
            .document(orderId.toString())
            .update("orderStatus", newStatus)
            .addOnSuccessListener {
                Log.d("SellerOrderDetail", "$collection order status updated successfully")
                _updateOrderStatusResult.value = Resource.Success(Unit)
            }
            .addOnFailureListener { exception ->
                Log.e("SellerOrderDetail", "Error updating $collection order status: $exception")
                _updateOrderStatusResult.value = Resource.Error("Failed to update $collection order status.")
            }
    }

    private fun updateOrderStatusInUserCollection(userId: String, orderId: Long, newStatus: String) {
        // Use the user's document reference to update the nested "orders" collection
        firestore.collection("user")
            .document(userId)
            .collection("orders")
            .document(orderId.toString())
            .update("orderStatus", newStatus)
            .addOnSuccessListener {
                Log.d("SellerOrderDetail", "user/$userId/orders order status updated successfully")
                _updateOrderStatusResult.value = Resource.Success(Unit)
            }
            .addOnFailureListener { exception ->
                Log.e("SellerOrderDetail", "Error updating user/$userId/orders order status: $exception")
                _updateOrderStatusResult.value = Resource.Error("Failed to update user/$userId/orders order status.")
            }
    }
}