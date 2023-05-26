package com.example.mytracking

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.mytracking.databinding.ActivityLoginBinding
import com.example.mytracking.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val register = binding.buttonregis
        val login = binding.teksregister

        register.setOnClickListener { Toast.makeText(this@RegisterActivity, "Register Success", Toast.LENGTH_SHORT).show() }
        login.setOnClickListener {
            val moveIntent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(moveIntent)
        }
        onLogin()
        setupView()

    }

    private fun setupView(){
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
    private fun onLogin() {
        binding.buttonregis.setOnClickListener {
            val move = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(move)
        }
    }

}