package com.example.smd_project

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.io.ByteArrayOutputStream
import java.util.UUID

class SubmitComplaintActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var etComplaintTitle: EditText
    private lateinit var rlCategorySpinner: RelativeLayout
    private lateinit var tvSelectedCategory: TextView
    private lateinit var etDescription: EditText
    private lateinit var etLocation: EditText
    private lateinit var btnGPS: ImageView
    private lateinit var rlAttachMedia: RelativeLayout
    private lateinit var switchAnonymous: SwitchCompat
    private lateinit var btnSubmitComplaint: Button

    // Bottom Navigation
    private lateinit var navHome: LinearLayout
    private lateinit var navCategories: LinearLayout
    private lateinit var navSubmit: LinearLayout
    private lateinit var navMyList: LinearLayout
    private lateinit var navProfile: LinearLayout

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val categories = arrayOf(
        "Road & Infrastructure",
        "Street Lights",
        "Garbage Collection",
        "Water Supply",
        "Drainage",
        "Public Transport",
        "Parks & Recreation",
        "Noise Pollution",
        "Other"
    )

    private var selectedCategory: String = ""
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_complaint)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize views
        initializeViews()

        // Set up click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        etComplaintTitle = findViewById(R.id.etComplaintTitle)
        rlCategorySpinner = findViewById(R.id.rlCategorySpinner)
        tvSelectedCategory = findViewById(R.id.tvSelectedCategory)
        etDescription = findViewById(R.id.etDescription)
        etLocation = findViewById(R.id.etLocation)
        btnGPS = findViewById(R.id.btnGPS)
        rlAttachMedia = findViewById(R.id.rlAttachMedia)
        switchAnonymous = findViewById(R.id.switchAnonymous)
        btnSubmitComplaint = findViewById(R.id.btnSubmitComplaint)

        // Bottom Navigation
        navHome = findViewById(R.id.navHome)
        navCategories = findViewById(R.id.navCategories)
        navSubmit = findViewById(R.id.navSubmit)
        navMyList = findViewById(R.id.navMyList)
        navProfile = findViewById(R.id.navProfile)
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Category selector
        rlCategorySpinner.setOnClickListener {
            showCategoryDialog()
        }

        // GPS button
        btnGPS.setOnClickListener {
            Toast.makeText(this, "GPS Location - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        // Attach media
        rlAttachMedia.setOnClickListener {
            openImagePicker()
        }

        // Submit button
        btnSubmitComplaint.setOnClickListener {
            submitComplaint()
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
            // Already on submit screen
        }

        navMyList.setOnClickListener {
            val intent = Intent(this, MyComplaintsActivity::class.java)
            startActivity(intent)
        }

        navProfile.setOnClickListener {
            Toast.makeText(this, "Profile - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCategoryDialog() {
        AlertDialog.Builder(this)
            .setTitle("Select Category")
            .setItems(categories) { dialog, which ->
                selectedCategory = categories[which]
                tvSelectedCategory.text = selectedCategory
                tvSelectedCategory.setTextColor(resources.getColor(android.R.color.black, null))
            }
            .show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitComplaint() {
        val title = etComplaintTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val isAnonymous = switchAnonymous.isChecked

        // Validation
        if (title.isEmpty()) {
            etComplaintTitle.error = "Complaint title is required"
            etComplaintTitle.requestFocus()
            return
        }

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            etDescription.error = "Description is required"
            etDescription.requestFocus()
            return
        }

        // Get current user
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button while submitting
        btnSubmitComplaint.isEnabled = false
        btnSubmitComplaint.text = "Submitting..."

        // Upload image if selected
        if (selectedImageUri != null) {
            uploadImageAndSubmitComplaint(currentUser.uid, title, description, location, isAnonymous)
        } else {
            submitComplaintToDatabase(currentUser.uid, title, description, location, isAnonymous, null)
        }
    }

    private fun uploadImageAndSubmitComplaint(
        userId: String,
        title: String,
        description: String,
        location: String,
        isAnonymous: Boolean
    ) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)

            // Compress and convert to Base64
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)

            // Submit complaint with base64 image
            submitComplaintToDatabase(userId, title, description, location, isAnonymous, base64Image)

        } catch (e: Exception) {
            btnSubmitComplaint.isEnabled = true
            btnSubmitComplaint.text = "Submit Complaint"
            Toast.makeText(this, "Failed to process image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitComplaintToDatabase(
        userId: String,
        title: String,
        description: String,
        location: String,
        isAnonymous: Boolean,
        base64Image: String?
    ) {
        val complaintRef = database.getReference("complaints").push()
        val complaintId = complaintRef.key ?: return

        val complaintData = hashMapOf(
            "userId" to userId,
            "title" to title,
            "category" to selectedCategory,
            "description" to description,
            "location" to location,
            "status" to "Pending",
            "isAnonymous" to isAnonymous,
            "imageBase64" to (base64Image ?: ""),
            "createdAt" to ServerValue.TIMESTAMP,
            "updatedAt" to ServerValue.TIMESTAMP
        )

        complaintRef.setValue(complaintData)
            .addOnSuccessListener {
                // Create initial history entry
                val historyRef = complaintRef.child("history").push()
                val historyData = hashMapOf(
                    "status" to "Submitted",
                    "timestamp" to ServerValue.TIMESTAMP,
                    "note" to "Complaint submitted successfully"
                )

                historyRef.setValue(historyData)
                    .addOnSuccessListener {
                        // Save to local storage
                        saveComplaintLocally(complaintId, title, selectedCategory, description)

                        Toast.makeText(this, "Complaint submitted successfully!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                btnSubmitComplaint.isEnabled = true
                btnSubmitComplaint.text = "Submit Complaint"
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveComplaintLocally(complaintId: String, title: String, category: String, description: String) {
        val sharedPref = getSharedPreferences("LocalComplaints", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Store complaint data locally
        editor.putString("complaint_$complaintId", "$title|$category|$description|${System.currentTimeMillis()}")
        editor.apply()
    }
}