package com.example.nutraglow

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity() {

    private lateinit var totalAmountText: TextView
    private lateinit var paymentSummary: TextView
    private lateinit var khaltiPaymentButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Match with your XML IDs
        totalAmountText = findViewById(R.id.totalPayAmount)
        paymentSummary = findViewById(R.id.paymentSummary)
        khaltiPaymentButton = findViewById(R.id.khaltiPaymentButton)

        // Get data from intent
        val totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        val totalItems = intent.getIntExtra("TOTAL_ITEMS", 0)

        // Show total amount and item count
        paymentSummary.text = "You are paying for $totalItems item(s)"
        totalAmountText.text = "Total Amount: Rs. $totalAmount"

        // Payment handler
        khaltiPaymentButton.setOnClickListener {
            Toast.makeText(this, "Initiating Khalti Payment for Rs. $totalAmount", Toast.LENGTH_LONG).show()
            // TODO: Integrate Khalti SDK here
        }
    }
}
