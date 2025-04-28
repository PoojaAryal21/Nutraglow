package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var goToCartButton: Button
    private lateinit var logoutButton: Button
    private lateinit var adminButton: Button

    private var isGuestUser = false
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        isGuestUser = intent.getBooleanExtra("isGuest", false)

        goToCartButton = findViewById(R.id.goToCartButton)
        logoutButton = findViewById(R.id.logoutButton)
        adminButton = findViewById(R.id.adminButton)

        if (!isGuestUser) {
            checkIfAdmin()
        }

        goToCartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            intent.putExtra("isGuest", isGuestUser)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        adminButton.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }
    }

    private fun checkIfAdmin() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)
            databaseRef.get().addOnSuccessListener { snapshot ->
                val role = snapshot.child("role").value?.toString()?.lowercase()
                if (role == "admin") {
                    adminButton.visibility = View.VISIBLE
                    isAdmin = true
                } else {
                    adminButton.visibility = View.GONE
                }
            }.addOnFailureListener {
                adminButton.visibility = View.GONE
            }
        } else {
            adminButton.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isGuestUser && FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
}
