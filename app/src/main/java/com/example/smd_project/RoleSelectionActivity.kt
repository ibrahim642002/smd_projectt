package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var btnAdmin: RelativeLayout
    private lateinit var btnCustomer: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize views
        btnAdmin = findViewById(R.id.btnAdmin)
        btnCustomer = findViewById(R.id.btnCustomer)

        // Admin button click
        btnAdmin.setOnClickListener {
            saveRoleSelection("admin")
            navigateToLogin()
        }

        // Customer button click
        btnCustomer.setOnClickListener {
            saveRoleSelection("customer")
            navigateToLogin()
        }
    }

    private fun saveRoleSelection(role: String) {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_ROLE", role)
            apply()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}