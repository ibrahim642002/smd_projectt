package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MyComplaintsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnSettings: ImageView
    private lateinit var tabAll: TextView
    private lateinit var tabPending: TextView
    private lateinit var tabResolved: TextView
    private lateinit var llComplaintsList: LinearLayout
    private lateinit var tvNoComplaints: TextView

    // Bottom Navigation
    private lateinit var navHome: LinearLayout
    private lateinit var navCategories: LinearLayout
    private lateinit var navSubmit: LinearLayout
    private lateinit var navMyList: LinearLayout
    private lateinit var navProfile: LinearLayout

    private var currentFilter = "All"

    // TODO: This will be replaced with Firebase data
    private val sampleComplaints = listOf(
        Complaint("1", "Street Light Not Working", "Main Road, Block A", "Infrastructure", "In Progress", "#12345", "2 days ago"),
        Complaint("2", "Garbage Collection Issue", "Park Street", "Cleanliness", "Pending", "#12344", "5 days ago"),
        Complaint("3", "Road Repair Needed", "Highway Road", "Traffic", "Resolved", "#12343", "1 week ago"),
        Complaint("4", "Water Supply Problem", "Residential Area", "Water", "Rejected", "#12342", "2 weeks ago"),
        Complaint("5", "Broken Street Signboard", "City Center", "Safety", "Pending", "#12341", "3 weeks ago")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_complaints)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize views
        initializeViews()

        // Set up click listeners
        setupClickListeners()

        // Load complaints (will be from Firebase later)
        loadComplaints()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        btnSettings = findViewById(R.id.btnSettings)
        tabAll = findViewById(R.id.tabAll)
        tabPending = findViewById(R.id.tabPending)
        tabResolved = findViewById(R.id.tabResolved)
        llComplaintsList = findViewById(R.id.llComplaintsList)
        tvNoComplaints = findViewById(R.id.tvNoComplaints)

        // Bottom Navigation
        navHome = findViewById(R.id.navHome)
        navCategories = findViewById(R.id.navCategories)
        navSubmit = findViewById(R.id.navSubmit)
        navMyList = findViewById(R.id.navMyList)
        navProfile = findViewById(R.id.navProfile)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSettings.setOnClickListener {
            Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        // Filter tabs
        tabAll.setOnClickListener {
            selectTab("All")
        }

        tabPending.setOnClickListener {
            selectTab("Pending")
        }

        tabResolved.setOnClickListener {
            selectTab("Resolved")
        }

        // Bottom Navigation
        navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        navCategories.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
        }

        navSubmit.setOnClickListener {
            val intent = Intent(this, SubmitComplaintActivity::class.java)
            startActivity(intent)
        }

        navMyList.setOnClickListener {
            // Already on My List screen
        }

        navProfile.setOnClickListener {
            Toast.makeText(this, "Profile - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectTab(filter: String) {
        currentFilter = filter

        // Reset all tabs
        tabAll.setBackgroundResource(R.drawable.tab_unselected)
        tabAll.setTextColor(resources.getColor(R.color.purple_500, null))
        tabPending.setBackgroundResource(R.drawable.tab_unselected)
        tabPending.setTextColor(resources.getColor(R.color.purple_500, null))
        tabResolved.setBackgroundResource(R.drawable.tab_unselected)
        tabResolved.setTextColor(resources.getColor(R.color.purple_500, null))

        // Highlight selected tab
        when (filter) {
            "All" -> {
                tabAll.setBackgroundResource(R.drawable.tab_selected)
                tabAll.setTextColor(resources.getColor(android.R.color.white, null))
            }
            "Pending" -> {
                tabPending.setBackgroundResource(R.drawable.tab_selected)
                tabPending.setTextColor(resources.getColor(android.R.color.white, null))
            }
            "Resolved" -> {
                tabResolved.setBackgroundResource(R.drawable.tab_selected)
                tabResolved.setTextColor(resources.getColor(android.R.color.white, null))
            }
        }

        // Reload complaints with filter
        loadComplaints()
    }

    private fun loadComplaints() {
        // Clear existing complaints
        llComplaintsList.removeAllViews()

        // Filter complaints based on current filter
        val filteredComplaints = when (currentFilter) {
            "All" -> sampleComplaints
            "Pending" -> sampleComplaints.filter { it.status == "Pending" || it.status == "In Progress" }
            "Resolved" -> sampleComplaints.filter { it.status == "Resolved" }
            else -> sampleComplaints
        }

        // Update tab counts
        val allCount = sampleComplaints.size
        val pendingCount = sampleComplaints.count { it.status == "Pending" || it.status == "In Progress" }
        val resolvedCount = sampleComplaints.count { it.status == "Resolved" }

        tabAll.text = "All ($allCount)"
        tabPending.text = "Pending ($pendingCount)"
        tabResolved.text = "Resolved ($resolvedCount)"

        // Show/hide no complaints message
        if (filteredComplaints.isEmpty()) {
            tvNoComplaints.visibility = TextView.VISIBLE
            return
        } else {
            tvNoComplaints.visibility = TextView.GONE
        }

        // Add complaint cards
        for (complaint in filteredComplaints) {
            addComplaintCard(complaint)
        }
    }

    private fun addComplaintCard(complaint: Complaint) {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.item_complaint, llComplaintsList, false)

        // Set data
        cardView.findViewById<TextView>(R.id.tvComplaintTitle).text = complaint.title
        cardView.findViewById<TextView>(R.id.tvLocation).text = complaint.location
        cardView.findViewById<TextView>(R.id.tvCategory).text = complaint.category
        cardView.findViewById<TextView>(R.id.tvComplaintId).text = complaint.id
        cardView.findViewById<TextView>(R.id.tvTimeAgo).text = complaint.timeAgo

        // Set status with appropriate background
        val tvStatus = cardView.findViewById<TextView>(R.id.tvStatus)
        tvStatus.text = complaint.status
        when (complaint.status) {
            "In Progress" -> tvStatus.setBackgroundResource(R.drawable.status_in_progress)
            "Pending" -> tvStatus.setBackgroundResource(R.drawable.status_pending)
            "Resolved" -> tvStatus.setBackgroundResource(R.drawable.status_resolved)
            "Rejected" -> tvStatus.setBackgroundResource(R.drawable.status_rejected)
        }

        // View details click
        cardView.findViewById<TextView>(R.id.tvViewDetails).setOnClickListener {
            openComplaintDetails(complaint)
        }

        llComplaintsList.addView(cardView)
    }

    private fun openComplaintDetails(complaint: Complaint) {
        val intent = Intent(this, ComplaintDetailsActivity::class.java)
        intent.putExtra("COMPLAINT_ID", complaint.complaintId)
        intent.putExtra("COMPLAINT_TITLE", complaint.title)
        intent.putExtra("COMPLAINT_CATEGORY", complaint.category)
        intent.putExtra("COMPLAINT_LOCATION", complaint.location)
        intent.putExtra("COMPLAINT_STATUS", complaint.status)
        intent.putExtra("COMPLAINT_DESCRIPTION", "Sample description for ${complaint.title}")
        intent.putExtra("COMPLAINT_DATE", "Oct 1, 2025")
        startActivity(intent)
    }

    // TODO: This data class will be replaced with Firebase model
    data class Complaint(
        val id: String,
        val title: String,
        val location: String,
        val category: String,
        val status: String,
        val complaintId: String,
        val timeAgo: String
    )
}