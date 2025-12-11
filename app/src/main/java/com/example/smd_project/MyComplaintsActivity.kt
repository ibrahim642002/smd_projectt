package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

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

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var currentFilter = "All"
    private var allComplaints = mutableListOf<Complaint>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_complaints)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize views
        initializeViews()

        // Set up click listeners
        setupClickListeners()

        // Load complaints from Firebase
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

        // Display filtered complaints
        displayComplaints()
    }

    private fun loadComplaints() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            tvNoComplaints.visibility = TextView.VISIBLE
            return
        }

        val complaintsRef = database.getReference("complaints")
        complaintsRef.orderByChild("userId").equalTo(currentUser.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allComplaints.clear()

                    for (complaintSnapshot in snapshot.children) {
                        val id = complaintSnapshot.key ?: continue
                        val title = complaintSnapshot.child("title").getValue(String::class.java) ?: ""
                        val category = complaintSnapshot.child("category").getValue(String::class.java) ?: ""
                        val location = complaintSnapshot.child("location").getValue(String::class.java) ?: ""
                        val status = complaintSnapshot.child("status").getValue(String::class.java) ?: "Pending"
                        val description = complaintSnapshot.child("description").getValue(String::class.java) ?: ""
                        val createdAt = complaintSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L

                        allComplaints.add(
                            Complaint(
                                id, title, location, category, status,
                                "#${id.take(6)}", getTimeAgo(createdAt), description, createdAt
                            )
                        )
                    }

                    // Sort by most recent
                    allComplaints.sortByDescending { it.createdAt }

                    // Update tab counts
                    updateTabCounts()

                    // Display complaints
                    displayComplaints()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MyComplaintsActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateTabCounts() {
        val allCount = allComplaints.size
        val pendingCount = allComplaints.count { it.status == "Pending" || it.status == "In Progress" }
        val resolvedCount = allComplaints.count { it.status == "Resolved" }

        tabAll.text = "All ($allCount)"
        tabPending.text = "Pending ($pendingCount)"
        tabResolved.text = "Resolved ($resolvedCount)"
    }

    private fun displayComplaints() {
        // Clear existing complaints
        llComplaintsList.removeAllViews()

        // Filter complaints based on current filter
        val filteredComplaints = when (currentFilter) {
            "All" -> allComplaints
            "Pending" -> allComplaints.filter { it.status == "Pending" || it.status == "In Progress" }
            "Resolved" -> allComplaints.filter { it.status == "Resolved" }
            else -> allComplaints
        }

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
        cardView.findViewById<TextView>(R.id.tvComplaintId).text = complaint.complaintId
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
        intent.putExtra("COMPLAINT_ID", complaint.id)
        intent.putExtra("COMPLAINT_TITLE", complaint.title)
        intent.putExtra("COMPLAINT_CATEGORY", complaint.category)
        intent.putExtra("COMPLAINT_LOCATION", complaint.location)
        intent.putExtra("COMPLAINT_STATUS", complaint.status)
        intent.putExtra("COMPLAINT_DESCRIPTION", complaint.description)
        intent.putExtra("COMPLAINT_DATE", formatDate(complaint.createdAt))
        startActivity(intent)
    }

    private fun getTimeAgo(timestamp: Long): String {
        if (timestamp == 0L) return "Just now"

        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7

        return when {
            weeks > 0 -> "$weeks week${if (weeks > 1) "s" else ""} ago"
            days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
            else -> "Just now"
        }
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Unknown"
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    data class Complaint(
        val id: String,
        val title: String,
        val location: String,
        val category: String,
        val status: String,
        val complaintId: String,
        val timeAgo: String,
        val description: String,
        val createdAt: Long
    )
}