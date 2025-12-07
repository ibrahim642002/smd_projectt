package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmailPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnLoginTab: Button
    private lateinit var btnSignUpTab: Button
    private lateinit var tvForgotPassword: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize views
        etEmailPhone = findViewById(R.id.etEmailPhone)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnLoginTab = findViewById(R.id.btnLoginTab)
        btnSignUpTab = findViewById(R.id.btnSignUpTab)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        // Login button click
        btnLogin.setOnClickListener {
            val email = etEmailPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        // Sign Up tab click
        btnSignUpTab.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Forgot password click
        tvForgotPassword.setOnClickListener {
            val email = etEmailPhone.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show()
            } else {
                resetPassword(email)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        // Show loading
        btnLogin.isEnabled = false
        btnLogin.text = "Logging in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                btnLogin.isEnabled = true
                btnLogin.text = "Login"

                if (task.isSuccessful) {
                    // Login successful
                    val user = auth.currentUser
                    if (user != null) {
                        // Fetch user data from Realtime Database
                        fetchUserDataAndNavigate(user.uid)
                    }
                } else {
                    // Login failed
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun fetchUserDataAndNavigate(userId: String) {
        val userRef = database.getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("name").getValue(String::class.java) ?: "User"

                // Navigate to MainActivity
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("USER_NAME", userName)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
                finish()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                // Navigate anyway with default name
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("USER_NAME", "User")
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
                finish()
            }
        })
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Password reset email sent to $email",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to send reset email: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmailPhone.error = "Email is required"
            etEmailPhone.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailPhone.error = "Please enter a valid email"
            etEmailPhone.requestFocus()
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

        return true
    }
}