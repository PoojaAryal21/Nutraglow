package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrderTrackingActivity : AppCompatActivity() {

    private lateinit var trackingStatus: TextView
    private lateinit var orderDetails: TextView
    private lateinit var estimatedDeliveryText: TextView
    private lateinit var deliveryProgressBar: ProgressBar
    private lateinit var homeButton: Button
    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_tracking)

        trackingStatus = findViewById(R.id.trackingStatus)
        orderDetails = findViewById(R.id.orderDetails)
        estimatedDeliveryText = findViewById(R.id.estimatedDelivery)
        deliveryProgressBar = findViewById(R.id.deliveryProgressBar)
        homeButton = findViewById(R.id.goHomeButton)
        auth = FirebaseAuth.getInstance()

        val orderId = intent.getStringExtra("ORDER_ID")
        if (orderId.isNullOrEmpty()) {
            trackingStatus.text = getString(R.string.error_no_order_id)
            return
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("orders")
        fetchOrderById(orderId)

        homeButton.setOnClickListener {
            startActivity(Intent(this@OrderTrackingActivity, CustomerActivity::class.java))
            finish()
        }
    }

    private fun fetchOrderById(orderId: String) {
        databaseRef.child(orderId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trackingStatus.text = getString(R.string.error_order_not_found)
                    return
                }

                val status = snapshot.child("status").getValue(String::class.java) ?: "Unknown"
                val orderTime = snapshot.child("timestamp").getValue(String::class.java) ?: "N/A"
                val method = snapshot.child("paymentMethod").getValue(String::class.java) ?: "N/A"
                val name = snapshot.child("customerName").getValue(String::class.java) ?: "N/A"
                val address = snapshot.child("address").getValue(String::class.java) ?: "N/A"

                trackingStatus.text = "Status: $status"
                orderDetails.text = "Name: $name\nAddress: $address\nMethod: $method\nTime: $orderTime"
                estimatedDeliveryText.text = "Estimated Delivery: 3-5 business days"

                deliveryProgressBar.progress = when (status) {
                    "Confirmed" -> 25
                    "Dispatched" -> 50
                    "Out for Delivery" -> 75
                    "Delivered" -> 100
                    else -> 0
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OrderTrackingActivity, "Failed to load order", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
