package com.fajar.myemarket.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

@Parcelize
data class CartProduct(
    val product: Product,
    val quantity: Int,
    val selectedColor: Int? = null,
    val selectedSize: String? = null,

): Parcelable {
    constructor() : this(Product(), 1, null, null)
}