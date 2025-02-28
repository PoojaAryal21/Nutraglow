package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: ArrayList<Product>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var goToCartButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Product List and Adapter
        productList = ArrayList()
        productAdapter = ProductAdapter(productList,  isCart = false)
        recyclerView.adapter = productAdapter

        // Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("products")

        // Fetch Products from Firebase
        fetchProducts()

        // Admin Button to Add New Products
        val adminButton = findViewById<Button>(R.id.adminButton)
        adminButton.setOnClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }
        // Initialize "Go to Cart" Button
        val goToCartButton = findViewById<Button>(R.id.goToCartButton)
        goToCartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

    }

    private fun fetchProducts() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    if (product != null) {
                        productList.add(product)
                    }
                }
                productAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
