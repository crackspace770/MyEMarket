package com.fajar.myemarket.core.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fajar.myemarket.core.model.Product
import com.fajar.myemarket.databinding.CartProductItemBinding
import com.fajar.myemarket.databinding.SellerItemBinding

import com.fajar.myemarket.utils.getProductPrice

class SellerProductAdapter: RecyclerView.Adapter<SellerProductAdapter.SellerItemViewHolder>() {

    inner class SellerItemViewHolder( val binding: SellerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            binding.apply {
                Glide.with(itemView).load(product.images[0]).into(imageSpecialRvItem)
                tvSpecialProductName.text = product.name

                val priceAfterPercentage = product.offerPercentage.getProductPrice(product.price)
                tvSpecialProductPrice.text = "$ ${String.format("%.2f", priceAfterPercentage)}"
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellerItemViewHolder {
        return SellerItemViewHolder(
            SellerItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: SellerItemViewHolder, position: Int) {
        val cartProduct = differ.currentList[position]
        holder.bind(cartProduct)

        holder.itemView.setOnClickListener {
            onProductClick?.invoke(cartProduct)
        }


    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onProductClick: ((Product) -> Unit)? = null

}