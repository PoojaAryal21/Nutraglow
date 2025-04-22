package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PaymentFailedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_failed)

        val failureMessage = findViewById<TextView>(R.id.failureMessage)
        val tryAgainButton = findViewById<Button>(R.id.tryAgainButton)
        val homeButton = findViewById<Button>(R.id.goHomeButton)

        failureMessage.text = "Sorry, your payment could not be completed. Please try again."

        tryAgainButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
            finish()
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, CustomerActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
