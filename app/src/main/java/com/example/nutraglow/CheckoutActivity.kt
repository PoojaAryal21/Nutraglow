package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CheckoutActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var paymentOptionGroup: RadioGroup
    private lateinit var proceedButton: Button

    private var totalAmount: Double = 0.0
    private var totalItems: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        nameInput = findViewById(R.id.checkoutName)
        addressInput = findViewById(R.id.checkoutAddress)
        phoneInput = findViewById(R.id.checkoutPhone)
        emailInput = findViewById(R.id.checkoutEmail)
        paymentOptionGroup = findViewById(R.id.paymentMethodGroup)
        proceedButton = findViewById(R.id.confirmOrderBtn)

        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        totalItems = intent.getIntExtra("TOTAL_ITEMS", 0)

        proceedButton.setOnClickListener {
            val name = nameInput.text.toString()
            val address = addressInput.text.toString()
            val phone = phoneInput.text.toString()
            val email = emailInput.text.toString()

            val selectedOptionId = paymentOptionGroup.checkedRadioButtonId

            if (selectedOptionId == -1) {
                Toast.makeText(this, "Please select a payment option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val paymentMethod = findViewById<RadioButton>(selectedOptionId).text.toString()

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (paymentMethod == "Cash on Delivery") {
                placeOrder(name, address, phone, email, "Cash on Delivery", false)
            } else if (paymentMethod == "Pay with Khalti") {
                val intent = Intent(this, ManualPaymentActivity::class.java).apply {
                    putExtra("TOTAL_AMOUNT", totalAmount)
                    putExtra("TOTAL_ITEMS", totalItems)
                    putExtra("NAME", name)
                    putExtra("ADDRESS", address)
                    putExtra("PHONE", phone)
                    putExtra("EMAIL", email)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Unsupported payment method", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun placeOrder(
        name: String,
        address: String,
        phone: String,
        email: String,
        paymentMethod: String,
        isPaid: Boolean
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val orderId = FirebaseDatabase.getInstance().reference.push().key ?: System.currentTimeMillis().toString()
        val cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId).child("products")

        cartRef.get().addOnSuccessListener { snapshot ->
            val productIds = mutableListOf<String>()
            for (child in snapshot.children) {
                val productId = child.child("productId").getValue(String::class.java)
                productId?.let { productIds.add(it) }
            }

            val order = Order(
                orderId = orderId,
                userId = userId,
                customerName = name,
                address = address,
                phone = phone,
                email = email,
                paymentMethod = paymentMethod,
                totalAmount = totalAmount,
                totalItems = totalItems,
                status = if (isPaid) "Processing" else "Pending",
                productIds = productIds
            )

            FirebaseDatabase.getInstance().getReference("orders").child(orderId).setValue(order)
                .addOnSuccessListener {
                    PushNotificationUtil.createNotificationChannel(this)
                    PushNotificationUtil.showNotification(
                        this,
                        "Order Confirmed",
                        "Your order has been confirmed and is being processed."
                    )

                    Toast.makeText(this, "Order Confirmed. Tracking available.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, OrderTrackingActivity::class.java).apply {
                        putExtra("ORDER_ID", orderId)
                    })
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch cart items", Toast.LENGTH_SHORT).show()
        }
    }
}
