package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CategoriesActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var etSearchCategory: EditText

    // Category Cards
    private lateinit var cardInfrastructure: RelativeLayout
    private lateinit var cardCleanliness: RelativeLayout
    private lateinit var cardSafety: RelativeLayout
    private lateinit var cardElectricity: RelativeLayout
    private lateinit var cardWaterSupply: RelativeLayout
    private lateinit var cardTraffic: RelativeLayout
    private lateinit var cardNoise: RelativeLayout
    private lateinit var cardOther: RelativeLayout

    // Bottom Navigation
    private lateinit var navHome: LinearLayout
    private lateinit var navCategories: LinearLayout
    private lateinit var navSubmit: LinearLayout
    private lateinit var navMyList: LinearLayout
    private lateinit var navProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize views
        initializeViews()

        // Set up click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        etSearchCategory = findViewById(R.id.etSearchCategory)

        // Category Cards
        cardInfrastructure = findViewById(R.id.cardInfrastructure)
        cardCleanliness = findViewById(R.id.cardCleanliness)
        cardSafety = findViewById(R.id.cardSafety)
        cardElectricity = findViewById(R.id.cardElectricity)
        cardWaterSupply = findViewById(R.id.cardWaterSupply)
        cardTraffic = findViewById(R.id.cardTraffic)
        cardNoise = findViewById(R.id.cardNoise)
        cardOther = findViewById(R.id.cardOther)

        // Bottom Navigation
        navHome = findViewById(R.id.navHome)
        navCategories = findViewById(R.id.navCategories)
        navSubmit = findViewById(R.id.navSubmit)
        navMyList = findViewById(R.id.navMyList)
        navProfile = findViewById(R.id.navProfile)
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Category cards
        cardInfrastructure.setOnClickListener {
            openCategory("Infrastructure")
        }

        cardCleanliness.setOnClickListener {
            openCategory("Cleanliness")
        }

        cardSafety.setOnClickListener {
            openCategory("Safety")
        }

        cardElectricity.setOnClickListener {
            openCategory("Electricity")
        }

        cardWaterSupply.setOnClickListener {
            openCategory("Water Supply")
        }

        cardTraffic.setOnClickListener {
            openCategory("Traffic")
        }

        cardNoise.setOnClickListener {
            openCategory("Noise")
        }

        cardOther.setOnClickListener {
            openCategory("Other")
        }

        // Bottom Navigation
        navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        navCategories.setOnClickListener {
            // Already on categories screen
        }

        navSubmit.setOnClickListener {
            val intent = Intent(this, SubmitComplaintActivity::class.java)
            startActivity(intent)
        }

        navMyList.setOnClickListener {
            Toast.makeText(this, "My List - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        navProfile.setOnClickListener {
            Toast.makeText(this, "Profile - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCategory(categoryName: String) {
        Toast.makeText(this, "$categoryName category - Coming Soon", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to category details screen showing complaints for this category
    }
}