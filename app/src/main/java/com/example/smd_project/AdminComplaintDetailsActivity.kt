package com.example.smd_project

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

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
    private lateinit var ivComplaintImage: ImageView
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
        complaintId = intent.getStringExtra("COMPLAINT_ID") ?: ""

        if (complaintId.isNotEmpty()) {
            loadComplaintFromFirebase()
        } else {
            loadComplaintData()
        }

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
        ivComplaintImage = findViewById(R.id.ivComplaintImage)
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus)
        btnAddNote = findViewById(R.id.btnAddNote)
        btnMarkResolved = findViewById(R.id.btnMarkResolved)
    }

    private fun loadComplaintFromFirebase() {
        database.getReference("complaints").child(complaintId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userId = snapshot.child("userId").getValue(String::class.java) ?: ""
                        val title = snapshot.child("title").getValue(String::class.java) ?: "N/A"
                        val category = snapshot.child("category").getValue(String::class.java) ?: "N/A"
                        val location = snapshot.child("location").getValue(String::class.java) ?: "N/A"
                        currentStatus = snapshot.child("status").getValue(String::class.java) ?: "Pending"
                        val description = snapshot.child("description").getValue(String::class.java) ?: "No description available."
                        val imageBase64 = snapshot.child("imageBase64").getValue(String::class.java) ?: ""
                        val createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0L

                        // Set data
                        tvComplaintTitle.text = title
                        tvComplaintId.text = "ID: #${complaintId.take(8)}"
                        tvCategory.text = "Category: $category"
                        tvLocation.text = "Location: $location"
                        tvSubmittedDate.text = "Submitted: ${formatDate(createdAt)}"
                        tvDescription.text = description

                        // Set status badge
                        tvStatusBadge.text = currentStatus
                        updateStatusBadgeColor(currentStatus)

                        // Display image if available
                        if (imageBase64.isNotEmpty()) {
                            displayImage(imageBase64)
                        } else {
                            ivComplaintImage.visibility = View.GONE
                        }

                        // Fetch user details
                        if (userId.isNotEmpty()) {
                            fetchUserDetails(userId)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AdminComplaintDetailsActivity, "Error loading complaint", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadComplaintData() {
        userId = intent.getStringExtra("USER_ID") ?: ""
        val title = intent.getStringExtra("COMPLAINT_TITLE") ?: "N/A"
        val category = intent.getStringExtra("COMPLAINT_CATEGORY") ?: "N/A"
        val location = intent.getStringExtra("COMPLAINT_LOCATION") ?: "N/A"
        currentStatus = intent.getStringExtra("COMPLAINT_STATUS") ?: "Pending"
        val description = intent.getStringExtra("COMPLAINT_DESCRIPTION") ?: "No description available."
        val date = intent.getStringExtra("COMPLAINT_DATE") ?: "N/A"

        tvComplaintTitle.text = title
        tvComplaintId.text = "ID: #${complaintId.take(8)}"
        tvCategory.text = "Category: $category"
        tvLocation.text = "Location: $location"
        tvSubmittedDate.text = "Submitted: $date"
        tvDescription.text = description
        tvStatusBadge.text = currentStatus
        updateStatusBadgeColor(currentStatus)

        if (userId.isNotEmpty()) {
            fetchUserDetails(userId)
        }
    }

    private fun displayImage(base64String: String) {
        try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            if (bitmap != null) {
                ivComplaintImage.setImageBitmap(bitmap)
                ivComplaintImage.visibility = View.VISIBLE
            } else {
                ivComplaintImage.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ivComplaintImage.visibility = View.GONE
        }
    }

    private fun updateStatusBadgeColor(status: String) {
        when (status) {
            "Pending" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
            "In Progress" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
            "Resolved" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
            "Rejected" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
        }
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
        input.setPadding(50, 30, 50, 30)

        AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(input)
            .setPositiveButton("Add") { dialog, _ ->
                val note = input.text.toString().trim()
                if (note.isNotEmpty()) {
                    addAdminNote(note)
                } else {
                    Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show()
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
                updateStatusBadgeColor(newStatus)

                // Add to history
                addToHistory(newStatus, "Status updated by admin")

                Toast.makeText(this, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()

                // Send push notification to user
                sendPushNotification(
                    userId,
                    "Complaint Status Updated",
                    "Your complaint status has been updated to $newStatus"
                )
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

                // Send notification to user about the note
                sendPushNotification(
                    userId,
                    "Admin Note Added",
                    "Admin has added a note to your complaint"
                )
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

    private fun sendPushNotification(targetUserId: String, title: String, message: String) {
        // Step 1: Get user's FCM token from database
        database.getReference("users").child(targetUserId).child("fcmToken")
            .get()
            .addOnSuccessListener { snapshot ->
                val fcmToken = snapshot.getValue(String::class.java)

                if (fcmToken != null && fcmToken.isNotEmpty()) {
                    // Step 2: Send FCM notification using HTTP request
                    Thread {
                        sendFCMNotification(fcmToken, title, message)
                    }.start()
                } else {
                    // Fallback: Store notification in database
                    storeNotificationInDatabase(targetUserId, title, message)
                }
            }
            .addOnFailureListener {
                // Fallback: Store notification in database
                storeNotificationInDatabase(targetUserId, title, message)
            }
    }

    private fun sendFCMNotification(fcmToken: String, title: String, message: String) {
        try {
            val url = URL("https://fcm.googleapis.com/fcm/send")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "key=YOUR_SERVER_KEY") // TODO: Add your FCM server key
            connection.doOutput = true

            val jsonBody = JSONObject()
            jsonBody.put("to", fcmToken)

            val notification = JSONObject()
            notification.put("title", title)
            notification.put("body", message)
            notification.put("sound", "default")

            val data = JSONObject()
            data.put("complaintId", complaintId)
            data.put("type", "status_update")

            jsonBody.put("notification", notification)
            jsonBody.put("data", data)
            jsonBody.put("priority", "high")

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(jsonBody.toString())
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                runOnUiThread {
                    // Notification sent successfully
                }
            }

            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to database notification
            runOnUiThread {
                storeNotificationInDatabase(userId, title, message)
            }
        }
    }

    private fun storeNotificationInDatabase(targetUserId: String, title: String, message: String) {
        val notificationData = hashMapOf(
            "userId" to targetUserId,
            "title" to title,
            "message" to message,
            "complaintId" to complaintId,
            "timestamp" to ServerValue.TIMESTAMP,
            "read" to false
        )

        database.getReference("notifications").push().setValue(notificationData)
            .addOnSuccessListener {
                Toast.makeText(this, "Notification sent", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Unknown"
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}