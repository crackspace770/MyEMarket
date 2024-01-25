package com.fajar.myemarket.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

@Parcelize
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Float,
    val offerPercentage: Float? = null,
    val description: String? = null,
    val colors: List<Int>? = null,
    val sizes: List<String>? = null,
    val images: List<String>,
    val sellerName: String? = null,
    val sellerId: String? = null,
    var buyerName:String? = null,
    val itemOrderId:String? = null
): Parcelable {
    constructor():this("0","","",0f,images = emptyList())

}