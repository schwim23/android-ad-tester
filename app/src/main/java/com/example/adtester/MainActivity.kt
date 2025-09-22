package com.example.adtester

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        findViewById<LinearLayout>(R.id.cardVideoAd).setOnClickListener {
            startActivity(Intent(this, VideoAdActivity::class.java))
        }
        
        findViewById<LinearLayout>(R.id.cardBannerAd).setOnClickListener {
            startActivity(Intent(this, BannerAdActivity::class.java))
        }
    }
}