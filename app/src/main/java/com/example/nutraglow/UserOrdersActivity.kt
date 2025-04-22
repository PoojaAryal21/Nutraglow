package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserOrdersActivity : AppCompatActivity() {

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var ordersAdapter: OrdersAdapter
    private val ordersList = mutableListOf<Order>()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val ordersRef = FirebaseDatabase.getInstance().getReference("orders")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_orders)

        ordersRecyclerView = findViewById(R.id.userOrdersRecyclerView)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)

        ordersAdapter = OrdersAdapter(ordersList, isAdmin = false, showCancel = true, onCancelClick = { order ->
            confirmCancelOrder(order)
        }, onTrackClick = { order ->
            val intent = Intent(this, OrderTrackingActivity::class.java)
            intent.putExtra("ORDER_ID", order.orderId)
            startActivity(intent)
        })

        ordersRecyclerView.adapter = ordersAdapter
        fetchUserOrders()
    }

    private fun fetchUserOrders() {
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        ordersRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ordersList.clear()
                    for (orderSnapshot in snapshot.children) {
                        try {
                            val order = orderSnapshot.getValue(Order::class.java)
                            order?.let { ordersList.add(it) }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    ordersAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserOrdersActivity, "Failed to load orders: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun confirmCancelOrder(order: Order) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Order")
            .setMessage("Are you sure you want to cancel this order?")
            .setPositiveButton("Yes") { _, _ -> cancelOrder(order) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelOrder(order: Order) {
        ordersRef.child(order.orderId).child("status").setValue("Cancelled")
            .addOnSuccessListener {
                val statusEntry = OrderStatus(status = "Cancelled")
                ordersRef.child(order.orderId).child("statusHistory").push().setValue(statusEntry)
                Toast.makeText(this, "Order cancelled", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to cancel order", Toast.LENGTH_SHORT).show()
            }
    }
}
