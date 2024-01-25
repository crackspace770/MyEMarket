package com.fajar.myemarket.ui.seller.order

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
import javax.inject.Inject

@HiltViewModel
class SellerOrderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
):ViewModel() {

    private val _allOrders = MutableStateFlow<Resource<List<Order>>>(Resource.Unspecified())
    val allOrders = _allOrders.asStateFlow()

    init {
        getAllOrders()
    }

    private fun getAllOrders() {
        viewModelScope.launch {
            _allOrders.emit(Resource.Loading())
        }

        firestore.collection("orders").get()
            .addOnSuccessListener { querySnapshot ->
                val orders = querySnapshot.toObjects(Order::class.java)
                viewModelScope.launch {
                    _allOrders.emit(Resource.Success(orders))
                }
            }
            .addOnFailureListener { exception ->
                viewModelScope.launch {
                    _allOrders.emit(Resource.Error(exception.message.toString()))
                }
            }
    }

}