package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInButton = findViewById<Button>(R.id.signInButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        signInButton.setOnClickListener { v: View? ->
            val intent = Intent(
                this@MainActivity,
                SignInActivity::class.java
            )
            startActivity(intent)
        }

        registerButton.setOnClickListener { v: View? ->
            val intent = Intent(
                this@MainActivity,
                RegisterActivity::class.java
            )
            startActivity(intent)
        }
    }
}