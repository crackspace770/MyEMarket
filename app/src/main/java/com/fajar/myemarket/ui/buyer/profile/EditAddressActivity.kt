package com.fajar.myemarket.ui.buyer.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fajar.myemarket.databinding.ActivityEditAddressBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class EditAddressActivity:AppCompatActivity() {

    private lateinit var binding: ActivityEditAddressBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()


        retrieveData()
        onAction()


    }

    private fun retrieveData() {
        val userId = auth.currentUser!!.uid
        val dataUser = db.collection("user").document(userId).collection("address")

        dataUser.addSnapshotListener { snapshot, exception ->
            exception?.let {
                Log.d(TAG, it.message.toString())
                return@addSnapshotListener
            }
            snapshot?.let {
                for (document in it) {
                    val addressTitle = document.getString("adressTitle")
                    val fullName = document.getString("fullName")
                    val street = document.getString("street")
                    val phone = document.getString("phone")
                    val city = document.getString("city")
                    val state = document.getString("state")

                    binding.apply {
                        edAddressTitle.setText(addressTitle.orEmpty())
                        edFullName.setText(fullName.orEmpty())
                        edStreet.setText(street.orEmpty())
                        edPhone.setText(phone.orEmpty())
                        edCity.setText(city.orEmpty())
                        edState.setText(state.orEmpty())
                    }
                }
            }
        }
    }

    private fun updateData() {
        val userId = auth.currentUser!!.uid

        val nTitleAddress = binding.edAddressTitle.text.toString()
        val nFullName = binding.edFullName.text.toString()
        val nStreet = binding.edStreet.text.toString()
        val nPhone = binding.edPhone.text.toString()
        val nCity = binding.edCity.text.toString()
        val nState = binding.edState.text.toString()

        val updateData = mapOf(
            "adressTitle" to nTitleAddress,
            "fullName" to nFullName,
            "street" to nStreet,
            "phone" to nPhone,
            "city" to nCity,
            "state" to nState
        )

        db.collection("user").document(userId).collection("address")
            .document(userId) // Specify the document ID you want to update
            .update(updateData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Update Successful",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressbarAddress.visibility = View.GONE
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to Update Data",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressbarAddress.visibility = View.GONE
                }
            }
    }

    private fun onAction(){
        binding.buttonSave.setOnClickListener {
            updateData()
        }
    }

    companion object {
        const val TAG = "EditAddressActivity"
    }


}