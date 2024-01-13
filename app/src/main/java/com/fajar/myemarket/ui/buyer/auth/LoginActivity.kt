package com.fajar.myemarket.ui.buyer.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fajar.myemarket.R
import com.fajar.myemarket.preference.UserPreference
import com.fajar.myemarket.databinding.ActivityLoginBinding
import com.fajar.myemarket.ui.buyer.MainActivity
import com.fajar.myemarket.ui.seller.SellerActivity
import com.fajar.myemarket.utils.NetworkConnectionLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginActivity:AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var networkConnection: NetworkConnectionLiveData
    private var sharedPreference: UserPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        networkConnection = NetworkConnectionLiveData(this)
        networkConnection.observe(this) {isInternetAvailable ->
            if(isInternetAvailable) {
                setContentView(binding.root)
            } else{
                setContentView(R.layout.network_error)
            }
        }

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        sharedPreference = UserPreference(this)

        // Check if the user is already logged in
        val isLoggedIn = sharedPreference?.getPreferenceBoolean("isLoggedIn") ?: false
        if (isLoggedIn) {
            startAppropriateActivity()
        } else {
            login()
            onAction()
        }

   //     login()
   //     onAction()
    }

    private fun login() {
        binding.btnLogin.setOnClickListener {

            if (TextUtils.isEmpty(binding.etEmailLogin.text.toString())) {
                binding.etEmailLogin.error = "Masukkan email yang benar"
                return@setOnClickListener
            } else if (TextUtils.isEmpty(binding.etPasswordLogin.text.toString())) {
                binding.edPasswordLogin.error = "Masukkan kata sandi yang benar"
            } else {
                binding.progressCircular.root.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(
                    binding.etEmailLogin.text.toString().trim(),
                    binding.etPasswordLogin.text.toString()
                )
                    .addOnCompleteListener { it ->
                        binding.progressCircular.root.visibility = View.GONE
                        if (it.isSuccessful) {
                            // Save login state in SharedPreferences
                            sharedPreference?.saveBoolean("isLoggedIn", true)
                            startAppropriateActivity()
                        } else {
                            Toast.makeText(
                                this,
                                "Login gagal, silahkan coba lagi",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }
    }

    private fun startAppropriateActivity() {
        val userId = auth.currentUser?.uid
        val dataUser = db.collection("user").document(userId!!)
        dataUser.get()
            .addOnSuccessListener {
                val status = it.get("isSeller")
                if (status == true) {
                    startActivity(Intent(this, SellerActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }
    }


    private fun onAction(){
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

}