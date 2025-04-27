package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignInActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var signInButton: Button
    private lateinit var registerButton: Button
    private lateinit var forgotPasswordText: TextView
    private lateinit var resendEmailText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        signInButton = findViewById(R.id.signInButton)
        registerButton = findViewById(R.id.registerButton)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        resendEmailText = findViewById(R.id.resendEmailText)

        // Go to Register Page
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Sign In
        signInButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    if (user != null && user.isEmailVerified) {
                        val userId = user.uid
                        database.child(userId).get().addOnSuccessListener { snapshot ->
                            val role = snapshot.child("role").value.toString()
                            when (role.lowercase()) {
                                "admin" -> startActivity(Intent(this, AdminActivity::class.java))
                                "vendor" -> startActivity(Intent(this, VendorDashboardActivity::class.java))
                                "customer" -> startActivity(Intent(this, CustomerActivity::class.java))
                                else -> Toast.makeText(this, "Invalid role!", Toast.LENGTH_SHORT).show()
                            }
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_LONG).show()
                        resendEmailText.visibility = TextView.VISIBLE
                        mAuth.signOut()
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Forgot Password
        forgotPasswordText.setOnClickListener {
            val email = emailField.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email to reset password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Resend Email Verification
        resendEmailText.setOnClickListener {
            val user = mAuth.currentUser
            user?.sendEmailVerification()?.addOnSuccessListener {
                Toast.makeText(this, "Verification email sent again!", Toast.LENGTH_SHORT).show()
            }?.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to resend verification: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.signOut() // Logout on start
    }
}
