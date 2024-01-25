package com.fajar.myemarket.ui.seller.upload

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fajar.myemarket.R
import com.fajar.myemarket.core.model.Product
import com.fajar.myemarket.databinding.FragmentUploadBinding
import com.fajar.myemarket.preference.UserPreference
import com.fajar.myemarket.ui.buyer.auth.LoginActivity
import com.fajar.myemarket.utils.hideBottomNavView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

class UploadFragment: Fragment(R.layout.fragment_upload) {

    private lateinit var binding: FragmentUploadBinding
    val selectedColors = mutableListOf<Int>()
    private var selectedImages = mutableListOf<Uri>()
    val firestore = Firebase.firestore

    private lateinit var auth: FirebaseAuth
    private var db: FirebaseFirestore? = null
    private var sharedPreference: UserPreference? = null

    private val storage = Firebase.storage.reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentUploadBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.buttonColorPicker.setOnClickListener {
            ColorPickerDialog
                .Builder(requireContext())
                .setTitle("Product color")
                .setPositiveButton("Select", object : ColorEnvelopeListener {

                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        envelope?.let {
                            selectedColors.add(it.color)
                            updateColors()
                        }
                    }

                }).setNegativeButton("Cancel") { colorPicker, _ ->
                    colorPicker.dismiss()
                }.show()
        }

        val selectImagesActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data

                    //Multiple images selected
                    if (intent?.clipData != null) {
                        val count = intent.clipData?.itemCount ?: 0
                        (0 until count).forEach {
                            val imagesUri = intent.clipData?.getItemAt(it)?.uri
                            imagesUri?.let { selectedImages.add(it) }
                        }

                        //One images was selected
                    } else {
                        val imageUri = intent?.data
                        imageUri?.let { selectedImages.add(it) }
                    }
                    updateImages()
                }
            }
        //6
        binding.buttonImagesPicker.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "image/*"
            selectImagesActivityResult.launch(intent)
        }

        binding.btnUpload.setOnClickListener {
            val productValidation = validateInformation()
            if (!productValidation) {
                Toast.makeText(requireContext(), "Check your inputs", Toast.LENGTH_SHORT).show()
            }
            saveProducts() {
                Log.d("test", it.toString())
            }
        }

        sharedPreference = UserPreference(context = requireContext())

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()



    }



    private fun validateInformation(): Boolean {
        if (selectedImages.isEmpty())
            return false
        if (binding.edName.text.toString().trim().isEmpty())
            return false
        if (binding.edCategory.text.toString().trim().isEmpty())
            return false
        if (binding.edPrice.text.toString().trim().isEmpty())
            return false
        return true
    }

    private fun saveProducts(state: (Boolean) -> Unit) {
        val sizes = getSizesList(binding.edSizes.text.toString().trim())
        val imagesByteArrays = getImagesByteArrays()
        val name = binding.edName.text.toString().trim()
        val images = mutableListOf<String>()
        val category = binding.edCategory.text.toString().trim()
        val productDescription = binding.edDescription.text.toString().trim()
        val price = binding.edPrice.text.toString().trim()
        val offerPercentage = binding.edOfferPercentage.text.toString().trim()

        // Get the current user's UID
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            lifecycleScope.launch {
                showLoading()

                try {
                    async {
                        imagesByteArrays.forEach {
                            val id = UUID.randomUUID().toString()
                            launch {
                                val imagesStorage = storage.child("products/images/$id")
                                val result = imagesStorage.putBytes(it).await()
                                val downloadUrl = result.storage.downloadUrl.await().toString()
                                images.add(downloadUrl)
                            }
                        }
                    }.await()
                } catch (e: Exception) {
                    hideLoading()
                    state(false)
                    return@launch
                }

                // Fetch the seller's name from the user collection
                firestore.collection("user").document(userId).get().addOnSuccessListener { userSnapshot ->
                    val sellerName = userSnapshot.getString("name")
                    val sellerId = userId

                    // Create a product object
                    val product = Product(
                        UUID.randomUUID().toString(),
                        name,
                        category,
                        price.toFloat(),
                        if (offerPercentage.isEmpty()) null else offerPercentage.toFloat(),
                        if (productDescription.isEmpty()) null else productDescription,
                        selectedColors,
                        sizes,
                        images,
                        sellerName,
                        sellerId// Include the seller's name in the product data
                    )

                    // Upload product data to "Products" collection
                    firestore.collection("Products").add(product).addOnSuccessListener { documentReference ->
                        // Get the newly created product's document ID
                        val productId = documentReference.id

                        // Upload product data to the subcollection "item" under the "User" collection
                        firestore.collection("user").document(userId).collection("item").document(productId)
                            .set(product)
                            .addOnSuccessListener {
                                state(true)
                                hideLoading()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Firestore", "Error adding document to subcollection: $exception")
                                state(false)
                                hideLoading()
                            }

                    }.addOnFailureListener {
                        Log.e("Firestore", "Error adding document to 'Products' collection: $it")
                        state(false)
                        hideLoading()
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Firestore", "Error fetching user data: $exception")
                    state(false)
                    hideLoading()
                }
            }
        } else {
            state(false)
            hideLoading()
            Log.e("Firestore", "User ID is null")
        }
    }

    private fun hideLoading() {
        binding.progressbar.visibility = View.INVISIBLE
    }

    private fun showLoading() {
        binding.progressbar.visibility = View.VISIBLE

    }

    private fun getImagesByteArrays(): List<ByteArray> {
        val imagesByteArray = mutableListOf<ByteArray>()
        selectedImages.forEach {
            val stream = ByteArrayOutputStream()
            val imageBmp = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
            if (imageBmp.compress(Bitmap.CompressFormat.JPEG, 85, stream)) {
                val imageAsByteArray = stream.toByteArray()
                imagesByteArray.add(imageAsByteArray)
            }
        }
        return imagesByteArray
    }

    private fun getSizesList(sizes: String): List<String>? {
        if (sizes.isEmpty())
            return null
        val sizesList = sizes.split(",").map { it.trim() }
        return sizesList
    }

    private fun updateColors() {
        var colors = ""
        selectedColors.forEach {
            colors = "$colors ${Integer.toHexString(it)}, "
        }
        binding.tvSelectedColors.text = colors
    }

    private fun updateImages() {
        binding.tvSelectedImages.setText(selectedImages.size.toString())
    }


}