package com.example.smd_project

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

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
    private lateinit var ivComplaintImage: ImageView
    private lateinit var llAdminNotes: LinearLayout
    private lateinit var tvNotesTitle: TextView

    // Firebase
    private lateinit var database: FirebaseDatabase
    private var complaintId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaint_details)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()

        // Initialize views
        initializeViews()

        // Get complaint ID and load full data from Firebase
        complaintId = intent.getStringExtra("COMPLAINT_ID") ?: ""

        if (complaintId.isNotEmpty()) {
            loadComplaintFromFirebase(complaintId)
        } else {
            // Fallback to intent data
            loadComplaintData()
        }

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
        ivComplaintImage = findViewById(R.id.ivComplaintImage)
        llAdminNotes = findViewById(R.id.llAdminNotes)
        tvNotesTitle = findViewById(R.id.tvNotesTitle)
    }

    private fun loadComplaintFromFirebase(complaintId: String) {
        database.getReference("complaints").child(complaintId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val title = snapshot.child("title").getValue(String::class.java) ?: "N/A"
                        val category = snapshot.child("category").getValue(String::class.java) ?: "N/A"
                        val location = snapshot.child("location").getValue(String::class.java) ?: "N/A"
                        val status = snapshot.child("status").getValue(String::class.java) ?: "Pending"
                        val description = snapshot.child("description").getValue(String::class.java) ?: "No description available."
                        val imageBase64 = snapshot.child("imageBase64").getValue(String::class.java) ?: ""
                        val createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0L

                        // Set data to views
                        tvComplaintTitle.text = title
                        tvComplaintId.text = "Complaint ID: #${complaintId.take(8)}"
                        tvCategory.text = "Category: $category"
                        tvLocation.text = "Location: $location"
                        tvSubmittedDate.text = "Submitted: ${formatDate(createdAt)}"
                        tvDescription.text = description

                        // Set status badge
                        tvStatusBadge.text = status
                        when (status) {
                            "In Progress" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
                            "Pending" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
                            "Resolved" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
                            "Rejected" -> tvStatusBadge.setBackgroundResource(R.drawable.status_badge_transparent)
                        }

                        // Display image if available
                        if (imageBase64.isNotEmpty()) {
                            displayImage(imageBase64)
                        } else {
                            ivComplaintImage.visibility = View.GONE
                        }

                        // Load admin notes
                        loadAdminNotes(snapshot)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ComplaintDetailsActivity, "Error loading complaint", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadComplaintData() {
        val title = intent.getStringExtra("COMPLAINT_TITLE") ?: "N/A"
        val category = intent.getStringExtra("COMPLAINT_CATEGORY") ?: "N/A"
        val location = intent.getStringExtra("COMPLAINT_LOCATION") ?: "N/A"
        val status = intent.getStringExtra("COMPLAINT_STATUS") ?: "Pending"
        val description = intent.getStringExtra("COMPLAINT_DESCRIPTION") ?: "No description available."
        val submittedDate = intent.getStringExtra("COMPLAINT_DATE") ?: "N/A"

        tvComplaintTitle.text = title
        tvComplaintId.text = "Complaint ID: $complaintId"
        tvCategory.text = "Category: $category"
        tvLocation.text = "Location: $location"
        tvSubmittedDate.text = "Submitted: $submittedDate"
        tvDescription.text = description
        tvStatusBadge.text = status
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

    private fun loadAdminNotes(snapshot: DataSnapshot) {
        llAdminNotes.removeAllViews()

        val notesSnapshot = snapshot.child("adminNotes")
        if (notesSnapshot.exists() && notesSnapshot.childrenCount > 0) {
            tvNotesTitle.visibility = View.VISIBLE

            for (noteSnapshot in notesSnapshot.children) {
                val note = noteSnapshot.child("note").getValue(String::class.java) ?: ""
                val timestamp = noteSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L

                if (note.isNotEmpty()) {
                    addNoteCard(note, timestamp)
                }
            }
        } else {
            tvNotesTitle.visibility = View.GONE
        }
    }

    private fun addNoteCard(note: String, timestamp: Long) {
        val noteView = LayoutInflater.from(this).inflate(
            R.layout.item_admin_note,
            llAdminNotes,
            false
        )

        noteView.findViewById<TextView>(R.id.tvNoteText).text = note
        noteView.findViewById<TextView>(R.id.tvNoteTime).text = formatDate(timestamp)

        llAdminNotes.addView(noteView)
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Unknown"
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
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