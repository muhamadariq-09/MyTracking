package com.example.mytracking.activity.dashboard

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import com.example.mytracking.DirectionActivity
import com.example.mytracking.activity.home.NavigationActivity
import com.example.mytracking.databinding.ActivityDetailAngkotBinding
import com.example.mytracking.models.Angkot

class DetailAngkotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailAngkotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailAngkotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btnTrack = binding.btnTrack

        btnTrack.setOnClickListener {
            startActivity(Intent(this, DirectionActivity::class.java))
        }

        val angkot = intent.getParcelableExtra<Angkot>("Angkot")
        if (angkot != null){
            val tvAngkot = binding.tvAngkot
            val tvJurusan = binding.tvJurusan


            tvAngkot.text = angkot.namaAngkot
            tvJurusan.text = angkot.jurusan
        }

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
}