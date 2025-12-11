package com.example.smd_project

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ComplaintDetailsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnMenu: ImageView
    private lateinit var tvStatusBadge: TextView
    private lateinit var tvComplaintTitle: TextView
    private lateinit var tvComplaintId: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvSubmittedDate: TextView
    private lateinit var tvDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaint_details)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize views
        initializeViews()

        // Get complaint data from intent
        loadComplaintData()

        // Set up click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        btnMenu = findViewById(R.id.btnMenu)
        tvStatusBadge = findViewById(R.id.tvStatusBadge)
        tvComplaintTitle = findViewById(R.id.tvComplaintTitle)
        tvComplaintId = findViewById(R.id.tvComplaintId)
        tvCategory = findViewById(R.id.tvCategory)
        tvLocation = findViewById(R.id.tvLocation)
        tvSubmittedDate = findViewById(R.id.tvSubmittedDate)
        tvDescription = findViewById(R.id.tvDescription)
    }

    private fun loadComplaintData() {
        // TODO: Get complaint ID from intent and fetch from Firebase
        val complaintId = intent.getStringExtra("COMPLAINT_ID") ?: ""
        val title = intent.getStringExtra("COMPLAINT_TITLE") ?: "N/A"
        val category = intent.getStringExtra("COMPLAINT_CATEGORY") ?: "N/A"
        val location = intent.getStringExtra("COMPLAINT_LOCATION") ?: "N/A"
        val status = intent.getStringExtra("COMPLAINT_STATUS") ?: "Pending"
        val description = intent.getStringExtra("COMPLAINT_DESCRIPTION") ?: "No description available."
        val submittedDate = intent.getStringExtra("COMPLAINT_DATE") ?: "N/A"

        // Set data to views
        tvComplaintTitle.text = title
        tvComplaintId.text = "Complaint ID: $complaintId"
        tvCategory.text = "Category: $category"
        tvLocation.text = "Location: $location"
        tvSubmittedDate.text = "Submitted: $submittedDate"
        tvDescription.text = description

        // Set status badge
        tvStatusBadge.text = status
        when (status) {
            "In Progress" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
            "Pending" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
            "Resolved" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
            "Rejected" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnMenu.setOnClickListener {
            Toast.makeText(this, "Menu options - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }
}