package com.example.mytracking.activity.profile

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.mytracking.activity.auth.LoginActivity
import com.example.mytracking.databinding.ActivityEditAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAccountBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Account"

        getUser()



        binding.btnSave.setOnClickListener {
            val name = binding.edtNama.text.toString()
            val email = binding.edtEmail.text.toString()
            val phone = binding.edtPhone.text.toString()
            val password = binding.edtPassword.text.toString()


            if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty()) {
                if (password.length >= 6) {

                    val hashMap: HashMap<String, Any?> = HashMap()

                    hashMap["name"] = name
                    hashMap["email"] = email
                    hashMap["phone"] = phone
                    hashMap["password"] = password

                    val dbRef = FirebaseDatabase.getInstance().getReference("Users")
                    dbRef.child(auth.uid!!)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Toast.makeText(this , "Account updated" , Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, AccountDetailActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this , "Failed to update account information" , Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this , "Password must be atleast 6 characters long", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this , "Empty Field are not allowed", Toast.LENGTH_SHORT).show()
            }

        }
        checkUser()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->

                startActivity(Intent(this, AccountDetailActivity::class.java))

        }
        return super.onOptionsItemSelected(item)
    }


    private fun getUser() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(auth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val password = "${snapshot.child("password").value}"
                    val phone = "${snapshot.child("phone").value}"



                    binding.edtEmail.setText(email)
                    binding.edtNama.setText(name)
                    binding.edtPassword.setText(password)
                    binding.edtPhone.setText(phone)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun checkUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

}