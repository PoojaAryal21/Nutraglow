package com.example.nutraglow

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AdminOrdersActivity : AppCompatActivity() {

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var ordersAdapter: OrdersAdapter
    private val ordersList = mutableListOf<Order>()
    private val fullOrdersList = mutableListOf<Order>()
    private val ordersRef = FirebaseDatabase.getInstance().getReference("orders")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_orders)

        // Initialize RecyclerView
        ordersRecyclerView = findViewById(R.id.adminOrdersRecyclerView)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersAdapter = OrdersAdapter(ordersList, isAdmin = true)
        ordersRecyclerView.adapter = ordersAdapter

        // Setup search input
        val searchInput = findViewById<EditText>(R.id.searchOrdersInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterOrders(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Load orders from Firebase
        fetchOrders()
    }

    private fun fetchOrders() {
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullOrdersList.clear()
                for (orderSnapshot in snapshot.children) {
                    try {
                        val order = orderSnapshot.getValue(Order::class.java)
                        if (order != null) {
                            fullOrdersList.add(order)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@AdminOrdersActivity,
                            "Error parsing order data: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                // Refresh visible list
                ordersList.clear()
                ordersList.addAll(fullOrdersList)
                ordersAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AdminOrdersActivity,
                    "Failed to load orders: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun filterOrders(query: String) {
        val lowerQuery = query.lowercase().trim()
        val filtered = fullOrdersList.filter {
            (it.customerName?.lowercase()?.contains(lowerQuery) == true) ||
                    (it.status?.lowercase()?.contains(lowerQuery) == true)
        }
        ordersList.clear()
        ordersList.addAll(filtered)
        ordersAdapter.notifyDataSetChanged()
    }
}
