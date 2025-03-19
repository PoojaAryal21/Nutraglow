package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SignInActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        val signInButton = findViewById<Button>(R.id.signInButton)
        registerButton = findViewById(R.id.registerButton)

        // Redirect to Register Activity
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        signInButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = mAuth.currentUser!!.uid
                    database.child(userId).get().addOnSuccessListener {
                        val role = it.child("role").value.toString()
                        when (role.lowercase()) {
                            "admin" -> startActivity(Intent(this, AdminActivity::class.java))
                            "vendor" -> startActivity(Intent(this, VendorActivity::class.java))
                            "customer" -> startActivity(Intent(this, CustomerActivity::class.java))
                            else -> Toast.makeText(this, "Invalid role!", Toast.LENGTH_SHORT).show()
                        }
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Login failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null) {
            mAuth.signOut() // Ensure user is logged out
        }
    }
}
