package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

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

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize views
        initializeViews()

        // Get user data from intent or local storage
        val userName = intent.getStringExtra("USER_NAME") ?: LocalStorageHelper.getUserName(this) ?: "User"
        userId = intent.getStringExtra("USER_ID") ?: LocalStorageHelper.getUserId(this) ?: auth.currentUser?.uid ?: ""
        tvUserName.text = userName

        // Save user data locally
        if (userId.isNotEmpty()) {
            LocalStorageHelper.saveUserData(this, userId, userName, "")
        }

        // Load cached statistics first (offline support)
        loadCachedStatistics()

        // Set up click listeners
        setupClickListeners()

        // Load complaints from Firebase (will update cached data)
        loadComplaintsFromFirebase()
    }

    private fun loadCachedStatistics() {
        val (total, pending, resolved) = LocalStorageHelper.getCachedStatistics(this)
        if (total > 0) {
            tvTotalCount.text = total.toString()
            tvPendingCount.text = pending.toString()
            tvResolvedCount.text = resolved.toString()
        }
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
            val intent = Intent(this, MyComplaintsActivity::class.java)
            startActivity(intent)
        }

        // Bottom Navigation
        navHome.setOnClickListener {
            // Already on home
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
        }

        // Notification click
        findViewById<RelativeLayout>(R.id.rlNotification).setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadComplaintsFromFirebase() {
        if (userId.isEmpty()) {
            showNoComplaints()
            return
        }

        val complaintsRef = database.getReference("complaints")
        complaintsRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Clear existing list
                    llComplaintsList.removeAllViews()

                    val complaints = mutableListOf<ComplaintData>()
                    var totalCount = 0
                    var pendingCount = 0
                    var resolvedCount = 0

                    // Parse complaints
                    for (complaintSnapshot in snapshot.children) {
                        val id = complaintSnapshot.key ?: continue
                        val title = complaintSnapshot.child("title").getValue(String::class.java) ?: ""
                        val category = complaintSnapshot.child("category").getValue(String::class.java) ?: ""
                        val location = complaintSnapshot.child("location").getValue(String::class.java) ?: ""
                        val status = complaintSnapshot.child("status").getValue(String::class.java) ?: "Pending"
                        val description = complaintSnapshot.child("description").getValue(String::class.java) ?: ""
                        val createdAt = complaintSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L

                        complaints.add(ComplaintData(id, title, category, location, status, description, createdAt))

                        // Count stats
                        totalCount++
                        when (status) {
                            "Pending", "In Progress" -> pendingCount++
                            "Resolved" -> resolvedCount++
                        }
                    }

                    // Update stats
                    tvTotalCount.text = totalCount.toString()
                    tvPendingCount.text = pendingCount.toString()
                    tvResolvedCount.text = resolvedCount.toString()

                    // Cache statistics locally
                    LocalStorageHelper.cacheStatistics(this@MainActivity, totalCount, pendingCount, resolvedCount)
                    LocalStorageHelper.updateLastSyncTime(this@MainActivity)

                    // Sort by most recent
                    complaints.sortByDescending { it.createdAt }

                    // Show recent complaints (max 3)
                    if (complaints.isEmpty()) {
                        showNoComplaints()
                    } else {
                        tvNoComplaints.visibility = TextView.GONE
                        complaints.take(3).forEach { complaint ->
                            addComplaintCard(complaint)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    showNoComplaints()
                }
            })
    }

    private fun addComplaintCard(complaint: ComplaintData) {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.item_complaint, llComplaintsList, false)

        // Set data
        cardView.findViewById<TextView>(R.id.tvComplaintTitle).text = complaint.title
        cardView.findViewById<TextView>(R.id.tvLocation).text = complaint.location
        cardView.findViewById<TextView>(R.id.tvCategory).text = complaint.category
        cardView.findViewById<TextView>(R.id.tvComplaintId).text = "#${complaint.id.take(6)}"
        cardView.findViewById<TextView>(R.id.tvTimeAgo).text = getTimeAgo(complaint.createdAt)

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

    private fun openComplaintDetails(complaint: ComplaintData) {
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

    private fun showNoComplaints() {
        tvNoComplaints.visibility = TextView.VISIBLE
        tvTotalCount.text = "0"
        tvPendingCount.text = "0"
        tvResolvedCount.text = "0"
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

    data class ComplaintData(
        val id: String,
        val title: String,
        val category: String,
        val location: String,
        val status: String,
        val description: String,
        val createdAt: Long
    )
}