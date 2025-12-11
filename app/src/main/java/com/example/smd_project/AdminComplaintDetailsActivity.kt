package com.example.smd_project

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class AdminComplaintDetailsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvStatusBadge: TextView
    private lateinit var tvComplaintTitle: TextView
    private lateinit var tvComplaintId: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvSubmittedDate: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnUpdateStatus: Button
    private lateinit var btnAddNote: Button
    private lateinit var btnMarkResolved: Button

    // Firebase
    private lateinit var database: FirebaseDatabase

    private var complaintId: String = ""
    private var userId: String = ""
    private var currentStatus: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_complaint_details)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()

        // Initialize views
        initializeViews()

        // Get data from intent
        loadComplaintData()

        // Set up click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        tvStatusBadge = findViewById(R.id.tvStatusBadge)
        tvComplaintTitle = findViewById(R.id.tvComplaintTitle)
        tvComplaintId = findViewById(R.id.tvComplaintId)
        tvCategory = findViewById(R.id.tvCategory)
        tvLocation = findViewById(R.id.tvLocation)
        tvSubmittedDate = findViewById(R.id.tvSubmittedDate)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvDescription = findViewById(R.id.tvDescription)
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus)
        btnAddNote = findViewById(R.id.btnAddNote)
        btnMarkResolved = findViewById(R.id.btnMarkResolved)
    }

    private fun loadComplaintData() {
        complaintId = intent.getStringExtra("COMPLAINT_ID") ?: ""
        userId = intent.getStringExtra("USER_ID") ?: ""
        val title = intent.getStringExtra("COMPLAINT_TITLE") ?: "N/A"
        val category = intent.getStringExtra("COMPLAINT_CATEGORY") ?: "N/A"
        val location = intent.getStringExtra("COMPLAINT_LOCATION") ?: "N/A"
        currentStatus = intent.getStringExtra("COMPLAINT_STATUS") ?: "Pending"
        val description = intent.getStringExtra("COMPLAINT_DESCRIPTION") ?: "No description available."
        val date = intent.getStringExtra("COMPLAINT_DATE") ?: "N/A"

        // Set data
        tvComplaintTitle.text = title
        tvComplaintId.text = "ID: #${complaintId.take(6)}"
        tvCategory.text = "Category: $category"
        tvLocation.text = "Location: $location"
        tvSubmittedDate.text = "Submitted: $date"
        tvDescription.text = description

        // Set status badge
        tvStatusBadge.text = currentStatus
        when (currentStatus) {
            "Pending" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
            "In Progress" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
            "Resolved" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
            "Rejected" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
        }

        // Fetch user details
        fetchUserDetails(userId)
    }

    private fun fetchUserDetails(userId: String) {
        database.getReference("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                val email = snapshot.child("email").getValue(String::class.java) ?: "N/A"

                tvUserName.text = "Submitted by: $name"
                tvUserEmail.text = "Email: $email"
            }
            .addOnFailureListener {
                tvUserName.text = "Submitted by: Unknown User"
                tvUserEmail.text = "Email: N/A"
            }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnUpdateStatus.setOnClickListener {
            showStatusUpdateDialog()
        }

        btnAddNote.setOnClickListener {
            showAddNoteDialog()
        }

        btnMarkResolved.setOnClickListener {
            updateStatus("Resolved")
        }
    }

    private fun showStatusUpdateDialog() {
        val statuses = arrayOf("Pending", "In Progress", "Resolved", "Rejected")

        AlertDialog.Builder(this)
            .setTitle("Update Status")
            .setItems(statuses) { dialog, which ->
                val newStatus = statuses[which]
                updateStatus(newStatus)
            }
            .show()
    }

    private fun showAddNoteDialog() {
        val input = EditText(this)
        input.hint = "Enter admin note..."

        AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(input)
            .setPositiveButton("Add") { dialog, _ ->
                val note = input.text.toString().trim()
                if (note.isNotEmpty()) {
                    addAdminNote(note)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateStatus(newStatus: String) {
        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "updatedAt" to ServerValue.TIMESTAMP
        )

        database.getReference("complaints").child(complaintId).updateChildren(updates)
            .addOnSuccessListener {
                currentStatus = newStatus
                tvStatusBadge.text = newStatus

                // Add to history
                addToHistory(newStatus, "Status updated by admin")

                Toast.makeText(this, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()

                // TODO: Send push notification to user
                sendNotificationToUser(userId, "Your complaint status has been updated to $newStatus")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addAdminNote(note: String) {
        val noteData = hashMapOf(
            "note" to note,
            "timestamp" to ServerValue.TIMESTAMP,
            "addedBy" to "admin"
        )

        database.getReference("complaints").child(complaintId)
            .child("adminNotes").push().setValue(noteData)
            .addOnSuccessListener {
                Toast.makeText(this, "Note added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add note", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addToHistory(status: String, note: String) {
        val historyData = hashMapOf(
            "status" to status,
            "note" to note,
            "timestamp" to ServerValue.TIMESTAMP
        )

        database.getReference("complaints").child(complaintId)
            .child("history").push().setValue(historyData)
    }

    private fun sendNotificationToUser(userId: String, message: String) {
        // TODO: Implement FCM push notification
        // For now, just create a notification record in database
        val notificationData = hashMapOf(
            "userId" to userId,
            "message" to message,
            "complaintId" to complaintId,
            "timestamp" to ServerValue.TIMESTAMP,
            "read" to false
        )

        database.getReference("notifications").push().setValue(notificationData)
    }
}