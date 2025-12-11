package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val splashDelay: Long = 2500 // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide the action bar if present
        supportActionBar?.hide()

        // Force the ImageView to scale properly
        val imageView = findViewById<ImageView>(R.id.ivClipboard)
        imageView.scaleType = ImageView.ScaleType.FIT_XY

        // Navigate to main activity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToMain()
        }, splashDelay)
    }

    private fun navigateToMain() {
        val intent = Intent(this, RoleSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }
}