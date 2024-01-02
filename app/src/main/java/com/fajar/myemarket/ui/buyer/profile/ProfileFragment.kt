package com.fajar.myemarket.ui.buyer.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.fajar.myemarket.databinding.FragmentProfileBinding
import com.fajar.myemarket.preference.UserPreference
import com.fajar.myemarket.ui.buyer.auth.LoginActivity
import com.fajar.myemarket.utils.loadImageUrl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment:Fragment() {

    private var binding: FragmentProfileBinding? = null
    private lateinit var auth: FirebaseAuth
    private var db: FirebaseFirestore? = null
    private var sharedPreference: UserPreference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreference = UserPreference(requireContext())

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

      //  initializeToolbar()
        retrieveData()
        onActions()
        logout()

    }



    private fun retrieveData(){

        val userId = auth.currentUser!!.uid
        val dataUser = db?.collection("user")?.document(userId)

        dataUser?.addSnapshotListener{ snapshot, exception ->
            exception?.let {
                Log.d(TAG, it.message.toString())
                return@addSnapshotListener
            }
            snapshot?.let {
                val fotoProfil = it.get("fotoProfil")
                val name = it.get("name")
                val email = it.get("email")
                val kelamin = it.get("kelamin")
                val phone = it.get("nomorHp")
                val alamat = it.get("alamat")

                binding?.apply {
                    ivPhoto.loadImageUrl(fotoProfil.toString(), requireContext())
                    tvNama.text = name.toString()
                    tvEmail.text = email.toString()
                    tvGender.text = kelamin.toString()
                    tvPhoneNumber.text = phone.toString()
                    tvAddress.text = alamat.toString()
                }

            }

        }

    }

    private fun onActions(){

        binding?.tvEditProfil?.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

    }

    private fun logout(){
        binding?.btnLogout?.setOnClickListener {
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                binding?.progressBarDialog?.root?.visibility = View.VISIBLE
                // Clear the shared preference for login status
                sharedPreference?.saveBoolean("isLoggedIn", false)
                // Logout from Firebase
                auth.signOut()
                // Redirect to LoginActivity
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                binding?.progressBarDialog?.root?.visibility = View.GONE
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        const val TAG = "MainActivity"
    }

}