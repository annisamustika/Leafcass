package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar

class DetailPenyakitActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ambil data penyakit dari Intent
        val penyakit = intent.getStringExtra("penyakit") ?: "Healthy"

        // Pilih layout sesuai penyakit
        val layoutId = when (penyakit) {
            "Bacterial Blight" -> R.layout.bacterialblight
            "Brown Spot" -> R.layout.brownspot
            "Green Mite" -> R.layout.greenmite
            "Healthy" -> R.layout.healthy
            "Mosaic" -> R.layout.mosaic
            else -> R.layout.healthy
        }

        setContentView(layoutId)
        title = penyakit

        // Pilih ID toolbar sesuai layout
        val toolbarId = when (penyakit) {
            "Bacterial Blight" -> R.id.toolbarBacterialBlight
            "Brown Spot" -> R.id.toolbarBrownSpot
            "Green Mite" -> R.id.toolbarGreenMite
            "Healthy" -> R.id.toolbarHealthy
            "Mosaic" -> R.id.toolbarMosaic
            else -> R.id.toolbarHealthy
        }

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(toolbarId)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
