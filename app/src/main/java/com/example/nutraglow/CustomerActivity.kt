package com.example.nutraglow

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CustomerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: MutableList<Product>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var logoutButton: Button  // ✅ Logout Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer) // ✅ Ensure this matches the XML file name

        recyclerView = findViewById(R.id.recyclerViewProduct) // ✅ Ensure this ID exists in XML
        recyclerView.layoutManager = LinearLayoutManager(this)

        productList = mutableListOf()
        productAdapter = ProductAdapter(productList, isCart = false)
        recyclerView.adapter = productAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("products")

        fetchProducts()

        logoutButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener { logoutUser() }
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
                Toast.makeText(this@CustomerActivity, "Failed to load products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        finish() // ✅ Close CustomerActivity and return to SignInActivity
    }
}
