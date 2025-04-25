package com.example.nutraglow

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class VendorPaymentsActivity : AppCompatActivity() {

    private lateinit var paymentsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_payments)

        paymentsText = findViewById(R.id.vendorPaymentsLabel)

        val vendorId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val productRef = FirebaseDatabase.getInstance().getReference("products")
        val paymentsRef = FirebaseDatabase.getInstance().getReference("payments")
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        productRef.orderByChild("owner").equalTo(vendorId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(productSnap: DataSnapshot) {
                    val vendorProductIds = productSnap.children.mapNotNull { it.key }.toSet()

                    paymentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(paySnap: DataSnapshot) {
                            val result = StringBuilder()

                            for (paymentData in paySnap.children) {
                                val amount = paymentData.child("amount").getValue(Double::class.java) ?: continue
                                val userId = paymentData.child("user").getValue(String::class.java)
                                val method = paymentData.child("method").getValue(String::class.java) ?: "N/A"
                                val productIdsSnapshot = paymentData.child("productIds")

                                val paidProductIds = productIdsSnapshot.children.mapNotNull { it.getValue(String::class.java) }

                                if (paidProductIds.any { it in vendorProductIds }) {
                                    // Nested async call to get user email
                                    userId?.let { uid ->
                                        usersRef.child(uid).child("email")
                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val email = snapshot.getValue(String::class.java) ?: "Unknown"
                                                    result.append("From: $email\nAmount: Rs. $amount\nMethod: $method\n\n")
                                                    paymentsText.text = result.toString()
                                                }

                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                    }
                                }
                            }

                            if (result.isEmpty()) {
                                paymentsText.text = "No payments found."
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            paymentsText.text = "Error loading payments: ${error.message}"
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    paymentsText.text = "Error loading vendor products: ${error.message}"
                }
            })
    }
}
