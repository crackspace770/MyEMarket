package com.fajar.myemarket.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name:String? = null,
    val email:String? = null,
    val kelamin: String? = null,
    val nomorHp:String? = null,
    val alamat:String? = null,
    val fotoProfil:String? = null,
    @field:JvmField
    val isSeller: Boolean? = false,
): Parcelable