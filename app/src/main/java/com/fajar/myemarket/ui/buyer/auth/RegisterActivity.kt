package com.fajar.myemarket.ui.buyer.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fajar.myemarket.R
import com.fajar.myemarket.databinding.ActivityRegisterBinding
import com.fajar.myemarket.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity:AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        register()
        onAction()
    }

    fun onRadioButtonClicked(view: View){
        if(view is RadioButton){
            val checked = view.isChecked

            when (view.id){
                R.id.rb_male ->
                    if(checked) {
                        GENDER = "Laki-laki"
                    }

                R.id.rb_female -> {
                    if (checked) {
                        GENDER = "Perempuan"
                    }
                }
            }
        }
    }

    private fun register(){

        val name = binding.edName.text
        val email = binding.edEmail.text
        val password = binding.edPassword.text
        val phone = binding.edPhone.text
        val alamat = binding.edAlamat.text

        binding.btnRegister.setOnClickListener {
            if (TextUtils.isEmpty(name.toString())) {
                binding.edName.error = "Masukkan nama yang benar"
                return@setOnClickListener
            } else if (TextUtils.isEmpty(email.toString())) {
                binding.edEmail.error = "Masukkan Email yang benar"
                return@setOnClickListener
            } else if (TextUtils.isEmpty(password.toString())) {
                binding.edPassword.error = "Masukkan kata sandi yang benar"
                return@setOnClickListener
            } else if(TextUtils.isEmpty(phone.toString())){
                binding.edPhone.error = "Masukkan nomor HP yang benar"
                return@setOnClickListener
            } else if(TextUtils.isEmpty(alamat.toString())){
                binding.edAlamat.error = "Masukkan alamat yang benar"
                return@setOnClickListener
            } else{
                binding.progressCircular.root.visibility = View.VISIBLE
                auth.createUserWithEmailAndPassword(
                    email.toString(),
                    password.toString()
                )
                    .addOnCompleteListener(this){
                        if (it.isSuccessful){
                            val photoUrl =
                                "https://ui-avatars.com/api/?background=8692F7&color=fff&size=100&rounded=true&name=$name"
                            val userId = auth.currentUser!!.uid
                            val data = User(
                                name.toString(),
                                email.toString(),
                                GENDER,
                                phone.toString(),
                                alamat.toString(),
                                photoUrl,
                                false
                                )

                            val userData = db.collection("user").document(userId)
                            userData.set(data)

                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()

                            Toast.makeText(
                                this,
                                "Berhasil mendaftar, silahkan lanjut masuk",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.progressCircular.root.visibility = View.GONE

                        } else{
                            Log.d("Register", it.toString())
                            Toast.makeText(
                                this,
                                "Gagal mendaftar, silahkan coba lagi",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.progressCircular.root.visibility = View.GONE

                        }
                    }



            }
        }

    }

    private fun onAction(){
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }


    companion object {
        var GENDER = ""
    }

}