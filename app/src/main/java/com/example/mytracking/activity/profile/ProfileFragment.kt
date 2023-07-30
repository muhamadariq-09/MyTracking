package com.example.mytracking.activity.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mytracking.activity.auth.LoginActivity
import com.example.mytracking.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()

        val logout = binding.logOut
        val detailProfile = binding.settingsAcc
        val aboutUs = binding.aboutUs
        val settings = binding.settingsApp



        settings.setOnClickListener {
            Toast.makeText(context , "On development, please choose another button" , Toast.LENGTH_SHORT).show()
        }

        logout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(context, LoginActivity::class.java))

        }

        detailProfile.setOnClickListener {
            startActivity(Intent(context, AccountDetailActivity::class.java))
        }

        aboutUs.setOnClickListener {
            startActivity(Intent(context, AboutUsActivity::class.java))
        }

        getUser()
        checkUser()



    }

    private fun getUser() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(auth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    binding.tvNama.text = name
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun checkUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(context, LoginActivity::class.java))
        }
    }





}