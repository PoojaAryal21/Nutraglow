package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class VendorOrdersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrdersAdapter
    private val vendorOrders = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_vendor_orders)

        recyclerView = findViewById(R.id.recyclerViewVendorOrders)
        recyclerView.layoutManager = LinearLayoutManager(this)
        orderAdapter = OrdersAdapter(vendorOrders, isAdmin = false, showCancel = false)
        recyclerView.adapter = orderAdapter

        loadOrdersForVendor(currentUser.uid)
    }

    private fun loadOrdersForVendor(vendorId: String) {
        val productRef = FirebaseDatabase.getInstance().getReference("products")
        val orderRef = FirebaseDatabase.getInstance().getReference("orders")

        productRef.orderByChild("owner").equalTo(vendorId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(productSnap: DataSnapshot) {
                    val vendorProductIds = productSnap.children.mapNotNull { it.key }.toSet()

                    orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(orderSnap: DataSnapshot) {
                            vendorOrders.clear()

                            for (orderData in orderSnap.children) {
                                try {
                                    val orderMap = orderData.value as? Map<*, *> ?: continue
                                    val rawProductIds = orderMap["productIds"] as? Map<*, *> // Firebase stores it like {0=abc, 1=xyz}
                                    val productIdList = rawProductIds?.values?.mapNotNull { it as? String } ?: emptyList()

                                    val order = orderData.getValue(Order::class.java)
                                    if (order != null && productIdList.any { it in vendorProductIds }) {
                                        vendorOrders.add(order.copy(productIds = productIdList))
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            orderAdapter.notifyDataSetChanged()

                            if (vendorOrders.isEmpty()) {
                                Toast.makeText(this@VendorOrdersActivity, "No matching orders found.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@VendorOrdersActivity, "Failed to load orders", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@VendorOrdersActivity, "Failed to load vendor products", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
