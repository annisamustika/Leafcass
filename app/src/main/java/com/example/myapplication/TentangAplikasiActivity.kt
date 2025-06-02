package com.example.myapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.google.android.material.appbar.MaterialToolbar

class TentangAplikasiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tentang_aplikasi)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarTentangAplikasi)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Tentang Aplikasi"
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))
        toolbar.setNavigationOnClickListener { finish() }
    }

}
