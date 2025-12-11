package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvTotalCount: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvPendingSmall: TextView
    private lateinit var tvInProgressCount: TextView
    private lateinit var tvResolvedCount: TextView
    private lateinit var llComplaintsList: LinearLayout
    private lateinit var tvViewAll: TextView

    // Bottom Navigation
    private lateinit var navDashboard: LinearLayout
    private lateinit var navComplaints: LinearLayout
    private lateinit var navUsers: LinearLayout
    private lateinit var navSettings: LinearLayout

    // Firebase
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

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
        tvTotalCount = findViewById(R.id.tvTotalCount)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvPendingSmall = findViewById(R.id.tvPendingSmall)
        tvInProgressCount = findViewById(R.id.tvInProgressCount)
        tvResolvedCount = findViewById(R.id.tvResolvedCount)
        llComplaintsList = findViewById(R.id.llComplaintsList)
        tvViewAll = findViewById(R.id.tvViewAll)

        // Bottom Navigation
        navDashboard = findViewById(R.id.navDashboard)
        navComplaints = findViewById(R.id.navComplaints)
        navUsers = findViewById(R.id.navUsers)
        navSettings = findViewById(R.id.navSettings)
    }

    private fun setupClickListeners() {
        tvViewAll.setOnClickListener {
            val intent = Intent(this, AdminComplaintsActivity::class.java)
            startActivity(intent)
        }

        // Bottom Navigation
        navDashboard.setOnClickListener {
            // Already on dashboard
        }

        navComplaints.setOnClickListener {
            val intent = Intent(this, AdminComplaintsActivity::class.java)
            startActivity(intent)
        }

        navUsers.setOnClickListener {
            Toast.makeText(this, "Users - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        navSettings.setOnClickListener {
            Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.btnSettings).setOnClickListener {
            Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAllComplaints() {
        val complaintsRef = database.getReference("complaints")

        complaintsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                llComplaintsList.removeAllViews()

                val complaints = mutableListOf<AdminComplaintData>()
                var totalCount = 0
                var pendingCount = 0
                var inProgressCount = 0
                var resolvedCount = 0

                for (complaintSnapshot in snapshot.children) {
                    val id = complaintSnapshot.key ?: continue
                    val userId = complaintSnapshot.child("userId").getValue(String::class.java) ?: ""
                    val title = complaintSnapshot.child("title").getValue(String::class.java) ?: ""
                    val category = complaintSnapshot.child("category").getValue(String::class.java) ?: ""
                    val location = complaintSnapshot.child("location").getValue(String::class.java) ?: ""
                    val status = complaintSnapshot.child("status").getValue(String::class.java) ?: "Pending"
                    val description = complaintSnapshot.child("description").getValue(String::class.java) ?: ""
                    val createdAt = complaintSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L

                    complaints.add(AdminComplaintData(id, userId, title, category, location, status, description, createdAt))

                    // Count stats
                    totalCount++
                    when (status) {
                        "Pending" -> pendingCount++
                        "In Progress" -> inProgressCount++
                        "Resolved" -> resolvedCount++
                    }
                }

                // Update stats
                tvTotalCount.text = totalCount.toString()
                tvPendingCount.text = pendingCount.toString()
                tvPendingSmall.text = pendingCount.toString()
                tvInProgressCount.text = inProgressCount.toString()
                tvResolvedCount.text = resolvedCount.toString()

                // Sort by most recent
                complaints.sortByDescending { it.createdAt }

                // Show recent 5 complaints
                complaints.take(5).forEach { complaint ->
                    addComplaintCard(complaint)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminDashboardActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
        intent.putExtra("COMPLAINT_DATE", formatDate(complaint.createdAt))
        startActivity(intent)
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Unknown"
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
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