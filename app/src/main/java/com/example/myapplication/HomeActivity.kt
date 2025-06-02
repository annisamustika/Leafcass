package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnTentang = findViewById<ImageView>(R.id.btnTentangAplikasi)
        btnTentang.setOnClickListener {
            val intent = Intent(this, TentangAplikasiActivity::class.java)
            startActivity(intent)
        }
        val startBtn = findViewById<Button>(R.id.startDetectionBtn)
        val infoBercakBtn = findViewById<LinearLayout>(R.id.infoBercakDaun)
        val infoBusukBtn = findViewById<LinearLayout>(R.id.infoBusukBatang)
        val infoMosaicBtn = findViewById<LinearLayout>(R.id.infoMosaicVirus)
        val infoLainBtn = findViewById<LinearLayout>(R.id.infoPenyakitLain1)
        val infoHealthyBtn = findViewById<LinearLayout>(R.id.infoHealthy)


        startBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Navigasi ke halaman detail penyakit
        infoBercakBtn.setOnClickListener {
            bukaDetailPenyakit("Green Mite")
        }

        infoBusukBtn.setOnClickListener {
            bukaDetailPenyakit("Brown Spot")
        }

        infoMosaicBtn.setOnClickListener {
            bukaDetailPenyakit("Bacterial Blight")
        }

        infoLainBtn.setOnClickListener {
            bukaDetailPenyakit("Mosaic")
        }

        infoHealthyBtn.setOnClickListener {
            bukaDetailPenyakit("Healthy")
        }
    }

    private fun bukaDetailPenyakit(namaPenyakit: String) {
        val intent = Intent(this, DetailPenyakitActivity::class.java)
        intent.putExtra("penyakit", namaPenyakit)
        startActivity(intent)
    }
}
