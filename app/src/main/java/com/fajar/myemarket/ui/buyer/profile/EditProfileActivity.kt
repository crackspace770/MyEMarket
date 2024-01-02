package com.fajar.myemarket.ui.buyer.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.fajar.myemarket.R
import com.fajar.myemarket.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class EditProfileActivity:AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: StorageReference
    private var imageUri: Uri? = null

    companion object{
        var NEW_GENDER =""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance().reference.child("photo/")

        retrieveData()
        onAction()
    }

    private fun retrieveData(){

        val userId = auth.currentUser!!.uid
        val dataUser = db.collection("user").document(userId)
        dataUser.get()
            .addOnSuccessListener {
                val fotoProfile = it.get("fotoProfil")
                val name = it.get("name")
                val gender = it.get("kelamin")
                val phone = it.get("nomorHp")
                val address = it.get("alamat")

                Glide.with(this)
                    .load(fotoProfile)
                    .centerCrop()
                    .into(binding.ivPhoto)

                binding.apply {
                    edNama.setText(name.toString())
                    edNoHp.setText(phone.toString())
                    edAlamat.setText(address.toString())
                    if (gender.toString() == "Laki-laki" ){
                        rbMale.isChecked = true
                        NEW_GENDER = "Laki-laki"
                    }else{
                        rbFemale.isChecked = true
                        NEW_GENDER = "Perempuan"

                    }
                }

            }

    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            when (view.id) {
                R.id.rb_male ->
                    if (checked) {
                        NEW_GENDER = "Laki-laki"
                    }

                R.id.rb_female -> {
                    if (checked) {
                        NEW_GENDER = "Perempuan"
                    }
                }
            }
        }
    }

    private fun onAction(){
        binding.apply {
            ivPhoto.setOnClickListener {
                resultLauncher.launch("image/*")
            }

            btnSimpan.setOnClickListener {
                if (imageUri != null) {
                    uploadImage()

                }    else{
                    updateData()
                }
            }
        }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){
        imageUri = it
        binding.ivPhoto.setImageURI(it)
    }

    private fun uploadImage(){

        if(imageUri !=null) {
            val userId = auth.currentUser!!.uid
            storage = storage.child(userId)

            // Kompres gambar sebelum mengunggah
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!))
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos)
            val data = baos.toByteArray()

            storage.putBytes(data)
                .addOnCompleteListener{task ->
                    if (task.isSuccessful){

                        storage.downloadUrl
                            .addOnSuccessListener { uri ->

                                val nFotoProfil = uri.toString()
                                val nName = binding.edNama.text.toString()
                                val nPhoneNumber = binding.edNoHp.toString()
                                val nAdress = binding.edAlamat.text.toString()

                                val updateData = mapOf(
                                    "fotoProfil" to nFotoProfil,
                                    "name" to nName,
                                    "kelamin" to NEW_GENDER,
                                    "nomorHp" to nPhoneNumber,
                                    "alamat" to nAdress
                                )

                                db.collection("user").document(userId).update(updateData)

                                Toast.makeText(this,"Data Tersimpan", Toast.LENGTH_SHORT).show()
                                binding.progressCircular.root.visibility = View.GONE
                                finish()
                            }

                            .addOnSuccessListener {
                                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()

                            }


                    }  else {
                        Toast.makeText(this,"Data Gagal Tersimpan", Toast.LENGTH_SHORT).show()
                        binding.progressCircular.root.visibility = View.GONE
                    }
                }
        }



    }

    private fun updateData() {
        val userId = auth.currentUser!!.uid

        val nName = binding.edNama.text.toString()
        val nPhoneNumber = binding.edNoHp.text.toString()
        val nAddress = binding.edAlamat.text.toString()

        val updateData = mapOf(
            "name" to nName,
            "kelamin" to NEW_GENDER,
            "nomorHp" to nPhoneNumber,
            "alamat" to nAddress
        )

        db.collection("user").document(userId).update(updateData)
            .addOnCompleteListener { task->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Sukses Menyimpan Data",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressCircular.root.visibility = View.GONE
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Gagal Menyimpan Data",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressCircular.root.visibility = View.GONE
                }
            }
    }

}