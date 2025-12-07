package com.example.smd_project

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.RelativeLayout
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvTotalCount: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvResolvedCount: TextView
    private lateinit var llComplaintsList: LinearLayout
    private lateinit var tvNoComplaints: TextView

    // Bottom Navigation
    private lateinit var navHome: LinearLayout
    private lateinit var navCategories: LinearLayout
    private lateinit var navMyList: LinearLayout
    private lateinit var navProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize views
        initializeViews()

        // Get user data from intent
        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        tvUserName.text = userName

        // Set up click listeners
        setupClickListeners()

        // TODO: Load complaints from Firebase
        // For now, show no complaints message
        showNoComplaints()
    }

    private fun initializeViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvTotalCount = findViewById(R.id.tvTotalCount)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvResolvedCount = findViewById(R.id.tvResolvedCount)
        llComplaintsList = findViewById(R.id.llComplaintsList)
        tvNoComplaints = findViewById(R.id.tvNoComplaints)

        // Bottom Navigation
        navHome = findViewById(R.id.navHome)
        navCategories = findViewById(R.id.navCategories)
        navMyList = findViewById(R.id.navMyList)
        navProfile = findViewById(R.id.navProfile)
    }

    private fun setupClickListeners() {
        // Quick Actions
        findViewById<RelativeLayout>(R.id.btnNewComplaint).setOnClickListener {
            val intent = Intent(this, SubmitComplaintActivity::class.java)
            startActivity(intent)
        }

        findViewById<RelativeLayout>(R.id.btnCategories).setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.tvViewAll).setOnClickListener {
            Toast.makeText(this, "View All clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to All Complaints screen
        }

        // Bottom Navigation
        navHome.setOnClickListener {
            // Already on home
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        navCategories.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
        }

        navMyList.setOnClickListener {
            val intent = Intent(this, MyComplaintsActivity::class.java)
            startActivity(intent)
        }

        navProfile.setOnClickListener {
            Toast.makeText(this, "Profile - Coming Soon", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Profile
        }

        // Notification click
        findViewById<RelativeLayout>(R.id.rlNotification).setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
            // TODO: Open notifications
        }
    }

    private fun showNoComplaints() {
        // Show placeholder message
        tvNoComplaints.visibility = TextView.VISIBLE

        // Set counts to 0
        tvTotalCount.text = "0"
        tvPendingCount.text = "0"
        tvResolvedCount.text = "0"
    }

    // TODO: Function to load complaints from Firebase
    private fun loadComplaintsFromFirebase() {
        // This will be implemented when Firebase is integrated
        // Will fetch complaints and populate the list
    }

    private fun openComplaintDetails(complaintId: String, title: String, category: String, location: String, status: String) {
        val intent = Intent(this, ComplaintDetailsActivity::class.java)
        intent.putExtra("COMPLAINT_ID", complaintId)
        intent.putExtra("COMPLAINT_TITLE", title)
        intent.putExtra("COMPLAINT_CATEGORY", category)
        intent.putExtra("COMPLAINT_LOCATION", location)
        intent.putExtra("COMPLAINT_STATUS", status)
        intent.putExtra("COMPLAINT_DESCRIPTION", "Sample description")
        intent.putExtra("COMPLAINT_DATE", "Oct 1, 2025")
        startActivity(intent)
    }
}