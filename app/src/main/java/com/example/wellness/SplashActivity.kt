package com.example.wellness

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val splashTimeOut: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide the action bar for full screen splash
        supportActionBar?.hide()

        // Use Handler to delay the transition to MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // Start MainActivity after splash timeout
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close splash activity
        }, splashTimeOut)
    }
}