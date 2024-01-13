package com.fajar.myemarket.ui.seller.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fajar.myemarket.core.model.Product
import com.fajar.myemarket.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerItemViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
):ViewModel() {

    private val _itemSeller = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val itemSeller = _itemSeller.asStateFlow()

    private var itemSellerDocuments = emptyList<DocumentSnapshot>()

    init {
        getSellerItem()
    }


    private fun getSellerItem() {
        viewModelScope.launch { _itemSeller.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("item")
            .addSnapshotListener { value, error ->
                if (error != null || value == null) {
                    viewModelScope.launch { _itemSeller.emit(Resource.Error(error?.message.toString())) }
                } else {
                    itemSellerDocuments = value.documents
                    val itemSeller = value.toObjects(Product::class.java)
                    viewModelScope.launch { _itemSeller.emit(Resource.Success(itemSeller)) }
                }
            }
    }
}