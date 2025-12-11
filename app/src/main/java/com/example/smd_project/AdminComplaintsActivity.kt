package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class AdminComplaintsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnFilter: ImageView
    private lateinit var tvComplaintsCount: TextView
    private lateinit var llComplaintsList: LinearLayout
    private lateinit var tvNoComplaints: TextView

    // Category tabs
    private lateinit var tabAll: TextView
    private lateinit var tabInfrastructure: TextView
    private lateinit var tabStreetLights: TextView
    private lateinit var tabGarbage: TextView
    private lateinit var tabWater: TextView
    private lateinit var tabOther: TextView

    // Bottom Navigation
    private lateinit var navDashboard: LinearLayout
    private lateinit var navComplaints: LinearLayout
    private lateinit var navUsers: LinearLayout
    private lateinit var navSettings: LinearLayout

    // Firebase
    private lateinit var database: FirebaseDatabase

    private var currentCategory = "All"
    private var allComplaints = mutableListOf<AdminComplaintData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_complaints)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()

        // Initialize views
        initializeViews()

        // Set up click listeners
        setupClickListeners()

        // Load all complaints
        loadAllComplaints()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        btnFilter = findViewById(R.id.btnFilter)
        tvComplaintsCount = findViewById(R.id.tvComplaintsCount)
        llComplaintsList = findViewById(R.id.llComplaintsList)
        tvNoComplaints = findViewById(R.id.tvNoComplaints)

        // Category tabs
        tabAll = findViewById(R.id.tabAll)
        tabInfrastructure = findViewById(R.id.tabInfrastructure)
        tabStreetLights = findViewById(R.id.tabStreetLights)
        tabGarbage = findViewById(R.id.tabGarbage)
        tabWater = findViewById(R.id.tabWater)
        tabOther = findViewById(R.id.tabOther)

        // Bottom Navigation
        navDashboard = findViewById(R.id.navDashboard)
        navComplaints = findViewById(R.id.navComplaints)
        navUsers = findViewById(R.id.navUsers)
        navSettings = findViewById(R.id.navSettings)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnFilter.setOnClickListener {
            Toast.makeText(this, "Filter options - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        // Category tabs
        tabAll.setOnClickListener { selectCategory("All") }
        tabInfrastructure.setOnClickListener { selectCategory("Road & Infrastructure") }
        tabStreetLights.setOnClickListener { selectCategory("Street Lights") }
        tabGarbage.setOnClickListener { selectCategory("Garbage Collection") }
        tabWater.setOnClickListener { selectCategory("Water Supply") }
        tabOther.setOnClickListener { selectCategory("Other") }

        // Bottom Navigation
        navDashboard.setOnClickListener {
            finish()
        }

        navComplaints.setOnClickListener {
            // Already on complaints
        }

        navUsers.setOnClickListener {
            Toast.makeText(this, "Users - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        navSettings.setOnClickListener {
            Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectCategory(category: String) {
        currentCategory = category

        // Reset all tabs
        val tabs = listOf(tabAll, tabInfrastructure, tabStreetLights, tabGarbage, tabWater, tabOther)
        tabs.forEach { tab ->
            tab.setBackgroundResource(R.drawable.tab_unselected)
            tab.setTextColor(resources.getColor(R.color.purple_500, null))
        }

        // Highlight selected tab
        val selectedTab = when (category) {
            "All" -> tabAll
            "Road & Infrastructure" -> tabInfrastructure
            "Street Lights" -> tabStreetLights
            "Garbage Collection" -> tabGarbage
            "Water Supply" -> tabWater
            "Other" -> tabOther
            else -> tabAll
        }

        selectedTab.setBackgroundResource(R.drawable.tab_selected)
        selectedTab.setTextColor(resources.getColor(android.R.color.white, null))

        // Display filtered complaints
        displayComplaints()
    }

    private fun loadAllComplaints() {
        val complaintsRef = database.getReference("complaints")

        complaintsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allComplaints.clear()

                for (complaintSnapshot in snapshot.children) {
                    val id = complaintSnapshot.key ?: continue
                    val userId = complaintSnapshot.child("userId").getValue(String::class.java) ?: ""
                    val title = complaintSnapshot.child("title").getValue(String::class.java) ?: ""
                    val category = complaintSnapshot.child("category").getValue(String::class.java) ?: ""
                    val location = complaintSnapshot.child("location").getValue(String::class.java) ?: ""
                    val status = complaintSnapshot.child("status").getValue(String::class.java) ?: "Pending"
                    val description = complaintSnapshot.child("description").getValue(String::class.java) ?: ""
                    val createdAt = complaintSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L

                    allComplaints.add(
                        AdminComplaintData(id, userId, title, category, location, status, description, createdAt)
                    )
                }

                // Sort by most recent
                allComplaints.sortByDescending { it.createdAt }

                // Display complaints
                displayComplaints()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminComplaintsActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayComplaints() {
        llComplaintsList.removeAllViews()

        // Filter by category
        val filteredComplaints = if (currentCategory == "All") {
            allComplaints
        } else {
            allComplaints.filter { it.category == currentCategory }
        }

        // Update count
        tvComplaintsCount.text = "Showing ${filteredComplaints.size} complaint${if (filteredComplaints.size != 1) "s" else ""}"

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

    private fun addComplaintCard(complaint: AdminComplaintData) {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.item_admin_complaint, llComplaintsList, false)

        // Set data
        cardView.findViewById<TextView>(R.id.tvComplaintTitle).text = complaint.title
        cardView.findViewById<TextView>(R.id.tvLocation).text = complaint.location
        cardView.findViewById<TextView>(R.id.tvComplaintId).text = "#${complaint.id.take(6)}"

        // Fetch user name
        fetchUserName(complaint.userId) { userName ->
            cardView.findViewById<TextView>(R.id.tvUserName).text = userName
        }

        // Set status
        val tvStatus = cardView.findViewById<TextView>(R.id.tvStatus)
        tvStatus.text = complaint.status
        when (complaint.status) {
            "Pending" -> tvStatus.setBackgroundResource(R.drawable.status_pending)
            "In Progress" -> tvStatus.setBackgroundResource(R.drawable.status_in_progress)
            "Resolved" -> tvStatus.setBackgroundResource(R.drawable.status_resolved)
            "Rejected" -> tvStatus.setBackgroundResource(R.drawable.status_rejected)
        }

        // Action buttons
        cardView.findViewById<Button>(R.id.btnApprove).setOnClickListener {
            updateComplaintStatus(complaint.id, "In Progress")
        }

        cardView.findViewById<Button>(R.id.btnReject).setOnClickListener {
            updateComplaintStatus(complaint.id, "Rejected")
        }

        cardView.findViewById<TextView>(R.id.tvView).setOnClickListener {
            openComplaintDetails(complaint)
        }

        llComplaintsList.addView(cardView)
    }

    private fun fetchUserName(userId: String, callback: (String) -> Unit) {
        database.getReference("users").child(userId).child("name")
            .get().addOnSuccessListener {
                callback(it.getValue(String::class.java) ?: "Unknown User")
            }.addOnFailureListener {
                callback("Unknown User")
            }
    }

    private fun updateComplaintStatus(complaintId: String, newStatus: String) {
        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "updatedAt" to com.google.firebase.database.ServerValue.TIMESTAMP
        )

        database.getReference("complaints").child(complaintId).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                // TODO: Send push notification to user
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openComplaintDetails(complaint: AdminComplaintData) {
        val intent = Intent(this, AdminComplaintDetailsActivity::class.java)
        intent.putExtra("COMPLAINT_ID", complaint.id)
        intent.putExtra("USER_ID", complaint.userId)
        intent.putExtra("COMPLAINT_TITLE", complaint.title)
        intent.putExtra("COMPLAINT_CATEGORY", complaint.category)
        intent.putExtra("COMPLAINT_LOCATION", complaint.location)
        intent.putExtra("COMPLAINT_STATUS", complaint.status)
        intent.putExtra("COMPLAINT_DESCRIPTION", complaint.description)
        startActivity(intent)
    }

    data class AdminComplaintData(
        val id: String,
        val userId: String,
        val title: String,
        val category: String,
        val location: String,
        val status: String,
        val description: String,
        val createdAt: Long
    )
}