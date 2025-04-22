package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrdersAdapter
    private val orderList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        recyclerView = findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OrdersAdapter(orderList, isAdmin = false, showCancel = false)
        recyclerView.adapter = adapter

        fetchOrderHistory()
    }

    private fun fetchOrderHistory() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ordersRef = FirebaseDatabase.getInstance().getReference("orders")

        ordersRef.orderByChild("userId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    orderList.clear()
                    for (orderSnapshot in snapshot.children) {
                        try {
                            val order = orderSnapshot.getValue(Order::class.java)
                            order?.let { orderList.add(it) }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@OrderHistoryActivity, "Failed to load orders", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, AccountActivity::class.java))
        finish()
    }
}
