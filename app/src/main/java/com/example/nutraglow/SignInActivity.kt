// Updated SignInActivity.kt with 10-second Toasts
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
    private lateinit var guestButton: Button

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
        guestButton = findViewById(R.id.guestButton)

        guestButton.setOnClickListener {
            val intent = Intent(this, CustomerActivity::class.java)
            intent.putExtra("isGuest", true)
            startActivity(intent)
            finish()
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        signInButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                showLongToast("All fields are required.")
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
                                else -> showLongToast("Invalid role!")
                            }
                            finish()
                        }
                    } else {
                        showLongToast("Please verify your email first.")
                        resendEmailText.visibility = TextView.VISIBLE
                        mAuth.signOut()
                    }
                } else {
                    showLongToast("Login failed: ${task.exception?.message}")
                }
            }
        }

        forgotPasswordText.setOnClickListener {
            val email = emailField.text.toString().trim()
            if (email.isEmpty()) {
                showLongToast("Please enter your email to reset password.")
                return@setOnClickListener
            }

            mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    showLongToast("Password reset email sent to $email")
                }
                .addOnFailureListener { e ->
                    showLongToast("Error: ${e.message}")
                }
        }

        resendEmailText.setOnClickListener {
            val user = mAuth.currentUser
            user?.sendEmailVerification()?.addOnSuccessListener {
                showLongToast("Verification email sent again!")
            }?.addOnFailureListener { e ->
                showLongToast("Failed to resend verification: ${e.message}")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.signOut()
    }

    private fun showLongToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
        android.os.Handler().postDelayed({ toast.show() }, 3500)
        android.os.Handler().postDelayed({ toast.show() }, 7000)
    }
}
