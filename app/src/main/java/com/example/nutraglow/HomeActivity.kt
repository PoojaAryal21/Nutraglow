package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await  // ✅ Import this!

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: ArrayList<Product>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var goToCartButton: Button
    private lateinit var logoutButton: Button  // ✅ Logout Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Product List and Adapter
        productList = ArrayList()
        productAdapter = ProductAdapter(productList, isCart = false)
        recyclerView.adapter = productAdapter

        // Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("products")

        // Fetch Products from Firebase using coroutines
        fetchProducts()

        // Admin Button to Add New Products
        val adminButton = findViewById<Button>(R.id.adminButton)
        adminButton.setOnClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }

        // Initialize "Go to Cart" Button
        goToCartButton = findViewById(R.id.goToCartButton)
        goToCartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        // ✅ Initialize Logout Button and Click Listener
        logoutButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener { logoutUser() }
    }

    // ✅ Optimized Fetch Products using Coroutines
    private fun fetchProducts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = databaseReference.get().await()
                withContext(Dispatchers.Main) {
                    productList.clear()
                    for (child in snapshot.children) {  // ✅ Fixed `it` issue
                        val product = child.getValue(Product::class.java)
                        if (product != null) {
                            productList.add(product)
                        }
                    }
                    productAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Failed to load products: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ✅ Optimized Logout Function with Coroutine
    private fun logoutUser() {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseAuth.getInstance().signOut()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@HomeActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@HomeActivity, SignInActivity::class.java)
                startActivity(intent)
                finish()  // ✅ Closes HomeActivity to prevent going back after logout
            }
        }
    }
}
