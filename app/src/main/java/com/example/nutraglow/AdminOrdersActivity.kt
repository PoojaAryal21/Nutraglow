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
                        val orderId = orderSnapshot.child("orderId").getValue(String::class.java) ?: continue
                        val userId = orderSnapshot.child("userId").getValue(String::class.java) ?: ""
                        val customerName = orderSnapshot.child("customerName").getValue(String::class.java) ?: ""
                        val address = orderSnapshot.child("address").getValue(String::class.java) ?: ""
                        val phone = orderSnapshot.child("phone").getValue(String::class.java) ?: ""
                        val email = orderSnapshot.child("email").getValue(String::class.java) ?: ""
                        val paymentMethod = orderSnapshot.child("paymentMethod").getValue(String::class.java) ?: ""
                        val totalAmount = orderSnapshot.child("totalAmount").getValue(Double::class.java) ?: 0.0
                        val totalItems = orderSnapshot.child("totalItems").getValue(Int::class.java) ?: 0
                        val status = orderSnapshot.child("status").getValue(String::class.java) ?: ""

                        val productIdsList = orderSnapshot.child("productIds").children.mapNotNull { it.getValue(String::class.java) }

                        val order = Order(
                            orderId = orderId,
                            userId = userId,
                            customerName = customerName,
                            address = address,
                            phone = phone,
                            email = email,
                            paymentMethod = paymentMethod,
                            totalAmount = totalAmount,
                            totalItems = totalItems,
                            status = status,
                            productIds = productIdsList
                        )
                        fullOrdersList.add(order)
                    } catch (e: Exception) {
                        e.printStackTrace() // Log the exception
                    }
                }

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
