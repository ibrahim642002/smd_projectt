package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.smd_project.R

class SplashActivity : AppCompatActivity() {

    private val splashDelay: Long = 2500 // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide the action bar if present
        supportActionBar?.hide()

        // Navigate to main activity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToMain()
        }, splashDelay)
    }

    private fun navigateToMain() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}