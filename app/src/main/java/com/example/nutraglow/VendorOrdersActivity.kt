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
                                // Manually extract fields
                                val orderId = orderData.child("orderId").getValue(String::class.java) ?: continue
                                val userId = orderData.child("userId").getValue(String::class.java) ?: continue
                                val customerName = orderData.child("customerName").getValue(String::class.java) ?: ""
                                val address = orderData.child("address").getValue(String::class.java) ?: ""
                                val phone = orderData.child("phone").getValue(String::class.java) ?: ""
                                val email = orderData.child("email").getValue(String::class.java) ?: ""
                                val paymentMethod = orderData.child("paymentMethod").getValue(String::class.java) ?: ""
                                val totalAmount = orderData.child("totalAmount").getValue(Double::class.java) ?: 0.0
                                val totalItems = orderData.child("totalItems").getValue(Int::class.java) ?: 0
                                val status = orderData.child("status").getValue(String::class.java) ?: ""

                                // Read productIds as a List<String> even if it's stored as a map
                                val productIdsList = orderData.child("productIds").children.mapNotNull { it.getValue(String::class.java) }

                                // Filter to vendor's products
                                val vendorProductIdsInOrder = productIdsList.filter { it in vendorProductIds }
                                if (vendorProductIdsInOrder.isNotEmpty()) {
                                    val updatedOrder = Order(
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
                                        productIds = vendorProductIdsInOrder
                                    )
                                    vendorOrders.add(updatedOrder)
                                }
                            }

                            orderAdapter.notifyDataSetChanged()

                            if (vendorOrders.isEmpty()) {
                                Toast.makeText(this@VendorOrdersActivity, "No orders for your products.", Toast.LENGTH_SHORT).show()
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
