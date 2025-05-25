package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.*
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
            if (!validateInputs()) return@setOnClickListener
            progressBar.visibility = ProgressBar.VISIBLE
            statusText.text = "Processing Payment..."
            processPayment()
        }

        cancelButton.setOnClickListener { finish() }
    }

    private fun validateInputs(): Boolean {
        val enteredPhone = paymentPhoneInput.text.toString()
        val cardNumber = cardNumberInput.text.toString()
        val expiryDate = expiryDateInput.text.toString()
        val cvv = cvvInput.text.toString()

        if (paymentNameInput.text.isBlank() || enteredPhone.isBlank() || cardNumber.isBlank() || expiryDate.isBlank() || cvv.isBlank()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (enteredPhone.length != 10 || !enteredPhone.all { it.isDigit() }) {
            Toast.makeText(this, "Phone number must be exactly 10 digits", Toast.LENGTH_SHORT).show()
            return false
        }

        if (cardNumber.length != 16 || !cardNumber.all { it.isDigit() }) {
            Toast.makeText(this, "Card number must be 16 digits", Toast.LENGTH_SHORT).show()
            return false
        }

        val sdf = SimpleDateFormat("MM/yy", Locale.US)
        try {
            val expDate = sdf.parse(expiryDate)
            val currentDate = Calendar.getInstance().time
            if (expDate == null || expDate.before(currentDate)) {
                Toast.makeText(this, "Card is expired", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid expiry date format. Use MM/yy", Toast.LENGTH_SHORT).show()
            return false
        }

        if (cvv.length != 3 || !cvv.all { it.isDigit() }) {
            Toast.makeText(this, "Invalid CVV", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun processPayment() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val orderId = FirebaseDatabase.getInstance().reference.push().key ?: System.currentTimeMillis().toString()
        val cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId).child("products")
        val productRef = FirebaseDatabase.getInstance().getReference("products")
        val paymentRef = FirebaseDatabase.getInstance().getReference("payments")
        val orderRef = FirebaseDatabase.getInstance().getReference("orders")

        cartRef.get().addOnSuccessListener { snapshot ->
            val productIds = mutableListOf<String>()
            val vendorPayments = mutableMapOf<String, Double>()
            val productStatusMap = mutableMapOf<String, String>()
            val productOwnerMap = mutableMapOf<String, String>()

            val children = snapshot.children.toList()
            if (children.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            var remaining = children.size

            for (item in children) {
                val productId = item.child("productId").getValue(String::class.java) ?: continue
                val price = item.child("price").getValue(Double::class.java) ?: 0.0
                productIds.add(productId)
                productStatusMap[productId] = "Confirmed"

                productRef.child(productId).child("owner").get().addOnSuccessListener { ownerSnap ->
                    val vendorId = ownerSnap.value?.toString() ?: "unknown"
                    productOwnerMap[productId] = vendorId
                    vendorPayments[vendorId] = (vendorPayments[vendorId] ?: 0.0) + price

                    remaining--
                    if (remaining == 0) {
                        val order = Order(
                            orderId = orderId,
                            userId = userId,
                            customerName = name,
                            address = address,
                            phone = phone,
                            email = email,
                            paymentMethod = "Paid via Card",
                            totalAmount = totalAmount,
                            totalItems = totalItems,
                            status = "Confirmed",
                            productIds = productIds,
                            productStatusMap = productStatusMap
                        )

                        orderRef.child(orderId).setValue(order)

                        for ((vendorId, amount) in vendorPayments) {
                            val vendorProductIds = productOwnerMap.filterValues { it == vendorId }.keys.toList()

                            val payment = mapOf(
                                "user" to userId,
                                "vendorId" to vendorId,
                                "amount" to amount,
                                "method" to "Paid via Card",
                                "productIds" to vendorProductIds,
                                "timestamp" to System.currentTimeMillis()
                            )

                            val paymentId = paymentRef.push().key ?: UUID.randomUUID().toString()
                            paymentRef.child(paymentId).setValue(payment)
                        }

                        FirebaseDatabase.getInstance().getReference("carts").child(userId).child("products").removeValue()

                        Toast.makeText(this, "Payment Successful. Thank you!", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, OrderTrackingActivity::class.java).apply {
                            putExtra("ORDER_ID", orderId)
                        })
                        finish()
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to retrieve cart items", Toast.LENGTH_SHORT).show()
        }
    }
}
