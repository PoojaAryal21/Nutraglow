package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AccountActivity : AppCompatActivity() {

    private lateinit var emailText: TextView
    private lateinit var changePasswordButton: Button
    private lateinit var navHome: Button
    private lateinit var navCart: Button
    private lateinit var navAccount: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        emailText = findViewById(R.id.accountEmail)
        changePasswordButton = findViewById(R.id.changePasswordButton)

        navHome = findViewById(R.id.navHome)
        navCart = findViewById(R.id.navCart)
        navAccount = findViewById(R.id.navAccount)

        val user = FirebaseAuth.getInstance().currentUser
        emailText.text = "Email: ${user?.email}"

        changePasswordButton.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(user?.email ?: "")
                .addOnSuccessListener {
                    Toast.makeText(this, "Reset link sent to your email.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to send reset link.", Toast.LENGTH_SHORT).show()
                }
        }

        navHome.setOnClickListener {
            startActivity(Intent(this, CustomerActivity::class.java))
        }

        navCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        navAccount.setOnClickListener {
            // already here
        }
    }
}
