package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class SignUpActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignUp: Button

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize views
        btnBack = findViewById(R.id.btnBack)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)

        // Back button
        btnBack.setOnClickListener {
            finish()
        }
        // Login link
        findViewById<TextView>(R.id.tvLogin).setOnClickListener {
            finish() // Go back to login screen
        }

        // Sign up button
        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                signUpUser(name, email, password)
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            etName.error = "Name is required"
            etName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email"
            etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return false
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Please confirm your password"
            etConfirmPassword.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return false
        }

        return true
    }

    private fun signUpUser(name: String, email: String, password: String) {
        btnSignUp.isEnabled = false
        btnSignUp.text = "Creating Account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up successful
                    val user = auth.currentUser
                    if (user != null) {
                        // Create user data in Realtime Database
                        createUserData(user.uid, name, email)
                    }
                } else {
                    btnSignUp.isEnabled = true
                    btnSignUp.text = "Sign Up"
                    Toast.makeText(
                        this,
                        "Sign up failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun createUserData(userId: String, name: String, email: String) {
        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to "",
            "createdAt" to ServerValue.TIMESTAMP
        )

        database.getReference("users").child(userId).setValue(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USER_NAME", name)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                btnSignUp.isEnabled = true
                btnSignUp.text = "Sign Up"
                Toast.makeText(this, "Error creating account: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}