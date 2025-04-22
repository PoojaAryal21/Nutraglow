package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ManualPaymentActivity : AppCompatActivity() {

    private lateinit var paymentNameInput: EditText
    private lateinit var paymentPhoneInput: EditText
    private lateinit var cardNumberInput: EditText
    private lateinit var expiryDateInput: EditText
    private lateinit var cvvInput: EditText
    private lateinit var payNowButton: Button
    private lateinit var cancelButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    private var totalAmount: Double = 0.0
    private var totalItems: Int = 0
    private lateinit var name: String
    private lateinit var address: String
    private lateinit var phone: String
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_payment)

        paymentNameInput = findViewById(R.id.paymentName)
        paymentPhoneInput = findViewById(R.id.paymentPhone)
        cardNumberInput = findViewById(R.id.cardNumberInput)
        expiryDateInput = findViewById(R.id.expiryDateInput)
        cvvInput = findViewById(R.id.cvvInput)
        payNowButton = findViewById(R.id.payNowButton)
        cancelButton = findViewById(R.id.cancelPaymentButton)
        progressBar = findViewById(R.id.paymentProgressBar)
        statusText = findViewById(R.id.paymentStatusText)

        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        totalItems = intent.getIntExtra("TOTAL_ITEMS", 0)
        name = intent.getStringExtra("NAME") ?: ""
        address = intent.getStringExtra("ADDRESS") ?: ""
        phone = intent.getStringExtra("PHONE") ?: ""
        email = intent.getStringExtra("EMAIL") ?: ""

        payNowButton.setOnClickListener {
            val enteredName = paymentNameInput.text.toString()
            val enteredPhone = paymentPhoneInput.text.toString()
            val cardNumber = cardNumberInput.text.toString()
            val expiryDate = expiryDateInput.text.toString()
            val cvv = cvvInput.text.toString()

            if (enteredName.isBlank() || enteredPhone.isBlank() || cardNumber.isBlank() || expiryDate.isBlank() || cvv.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (enteredPhone.length != 10 || !enteredPhone.all { it.isDigit() }) {
                Toast.makeText(this, "Phone number must be exactly 10 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cardNumber.length != 16 || !cardNumber.all { it.isDigit() }) {
                Toast.makeText(this, "Card number must be 16 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sdf = SimpleDateFormat("MM/yy", Locale.US)
            try {
                val expDate = sdf.parse(expiryDate)
                val currentDate = Calendar.getInstance().time
                if (expDate == null || expDate.before(currentDate)) {
                    Toast.makeText(this, "Card is expired", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid expiry date format. Use MM/yy", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cvv.length != 3 || !cvv.all { it.isDigit() }) {
                Toast.makeText(this, "Invalid CVV. Redirecting to failure page.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, PaymentFailedActivity::class.java))
                return@setOnClickListener
            }

            progressBar.visibility = ProgressBar.VISIBLE
            statusText.text = "Processing Payment..."

            placeOrder(name, address, phone, email, "Paid via Manual Entry")
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun placeOrder(
        name: String,
        address: String,
        phone: String,
        email: String,
        paymentMethod: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val orderId = FirebaseDatabase.getInstance().reference.push().key ?: System.currentTimeMillis().toString()

        val order = Order(
            orderId = orderId,
            userId = userId,
            customerName = name,
            address = address,
            phone = phone,
            email = email,
            paymentMethod = paymentMethod,
            totalAmount = totalAmount,
            status = "Confirmed"
        )

        FirebaseDatabase.getInstance().getReference("orders").child(orderId).setValue(order)
            .addOnSuccessListener {
                Toast.makeText(this, "Payment Successful. Tracking available.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, OrderTrackingActivity::class.java).apply {
                    putExtra("ORDER_ID", orderId)
                })
                finish()
            }
    }
}
