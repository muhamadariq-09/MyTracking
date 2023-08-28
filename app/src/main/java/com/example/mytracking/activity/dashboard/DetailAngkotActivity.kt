package com.example.mytracking.activity.dashboard

import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mytracking.DirectionActivity
import com.example.mytracking.databinding.ActivityDetailAngkotBinding
import com.example.mytracking.models.Angkot
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth


class DetailAngkotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailAngkotBinding
    private lateinit var auth: FirebaseAuth

    private var lat = 0.0
    private var lng = 0.0
    private var jur = "a"




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailAngkotBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val btnTrack = binding.btnTrack


        val angkotparcel = intent.getParcelableExtra<Angkot>("Angkot")


        btnTrack.setOnClickListener {
            val intent = Intent(this@DetailAngkotActivity, DirectionActivity::class.java)
            intent.putExtra("latitude", lat)
            intent.putExtra("longitude", lng)
            intent.putExtra("jurusan", angkotparcel?.jurusan)
            startActivity(intent)
        }

        val angkot = intent.getParcelableExtra<Angkot>("Angkot")
        if (angkot != null){
            val tvAngkot = binding.tvAngkot
            val tvJurusan = binding.tvJurusan
            val tvDeskripsi = binding.tvDescription


            tvAngkot.text = angkot.namaAngkot
            tvJurusan.text = angkot.jurusan
            tvDeskripsi.text = angkot.deskripsi
        }


        supportActionBar?.title = "Detail Angkot"
    }


}



