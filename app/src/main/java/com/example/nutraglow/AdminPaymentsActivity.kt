package com.example.nutraglow

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AdminPaymentsActivity : AppCompatActivity() {

    private lateinit var paymentsListView: ListView
    private lateinit var databasePayments: DatabaseReference
    private lateinit var databaseUsers: DatabaseReference
    private lateinit var removeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_payments)

        paymentsListView = findViewById(R.id.paymentsListView)
        removeButton = findViewById(R.id.removePaymentButton)
        databasePayments = FirebaseDatabase.getInstance().getReference("payments")
        databaseUsers = FirebaseDatabase.getInstance().getReference("users")

        val paymentDetails = mutableListOf<String>()
        val paymentKeys = mutableListOf<String>()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, paymentDetails)
        paymentsListView.choiceMode = ListView.CHOICE_MODE_SINGLE
        paymentsListView.adapter = adapter

        val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        databasePayments.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                paymentDetails.clear()
                paymentKeys.clear()

                for (paymentSnapshot in snapshot.children) {
                    val paymentKey = paymentSnapshot.key ?: continue
                    val customerId = paymentSnapshot.child("user").getValue(String::class.java)
                    val amount = paymentSnapshot.child("amount").getValue(Double::class.java) ?: 0.0
                    val vendorId = paymentSnapshot.child("vendorId").getValue(String::class.java)
                    val method = paymentSnapshot.child("method").getValue(String::class.java) ?: "N/A"
                    val timestamp = paymentSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    val formattedDate = dateFormatter.format(Date(timestamp))

                    if (customerId == null || vendorId == null) continue

                    // Fetch customer email
                    databaseUsers.child(customerId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(customerSnap: DataSnapshot) {
                            val customerEmail = customerSnap.child("email").getValue(String::class.java) ?: "unknown"

                            // Fetch vendor email
                            databaseUsers.child(vendorId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(vendorSnap: DataSnapshot) {
                                    val vendorEmail = vendorSnap.child("email").getValue(String::class.java) ?: "unknown"

                                    val entry = "Vendor: $vendorEmail | Customer: $customerEmail\n" +
                                            "Amount: Rs. $amount | Method: $method | Date: $formattedDate"
                                    paymentDetails.add(entry)
                                    paymentKeys.add(paymentKey)
                                    adapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminPaymentsActivity, "Failed to load payments.", Toast.LENGTH_SHORT).show()
            }
        })

        removeButton.setOnClickListener {
            val position = paymentsListView.checkedItemPosition
            if (position != ListView.INVALID_POSITION) {
                val selectedKey = paymentKeys[position]
                AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to remove this payment record?")
                    .setPositiveButton("Yes") { _, _ ->
                        databasePayments.child(selectedKey).removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Payment removed.", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to remove payment.", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(this, "Please select a payment to remove.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
