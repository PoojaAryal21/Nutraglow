package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class VendorDashboardActivity : AppCompatActivity() {

    private lateinit var databaseProducts: DatabaseReference
    private lateinit var vendorEmailText: TextView
    private lateinit var productName: EditText
    private lateinit var productPrice: EditText
    private lateinit var productDescription: EditText
    private lateinit var productImage: EditText
    private lateinit var addProductButton: Button
    private lateinit var logoutButton: Button
    private lateinit var viewOrdersButton: Button
    private lateinit var viewPaymentsButton: Button

    private lateinit var productRecyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val vendorProducts = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_dashboard)

        databaseProducts = FirebaseDatabase.getInstance().getReference("products")

        vendorEmailText = findViewById(R.id.vendorEmailText)
        productName = findViewById(R.id.productName)
        productPrice = findViewById(R.id.productPrice)
        productDescription = findViewById(R.id.productDescription)
        productImage = findViewById(R.id.productImage)
        addProductButton = findViewById(R.id.addProductButton)
        logoutButton = findViewById(R.id.logoutButton)
        viewOrdersButton = findViewById(R.id.viewOrdersButton)
        viewPaymentsButton = findViewById(R.id.viewPaymentsButton)

        productRecyclerView = findViewById(R.id.vendorProductRecyclerView)
        productRecyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter(vendorProducts, isCart = false, showDescription = false)
        productRecyclerView.adapter = productAdapter

        displayVendorEmail()
        fetchVendorProducts()

        addProductButton.setOnClickListener { addProduct() }
        logoutButton.setOnClickListener { logoutUser() }
        viewOrdersButton.setOnClickListener {
            startActivity(Intent(this, VendorOrdersActivity::class.java))
        }
        viewPaymentsButton.setOnClickListener {
            startActivity(Intent(this, VendorPaymentsActivity::class.java))
        }
    }

    private fun displayVendorEmail() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        userRef.child("email").get().addOnSuccessListener {
            vendorEmailText.text = "Vendor: ${it.value}"
        }
    }

    private fun fetchVendorProducts() {
        val vendorId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        databaseProducts.orderByChild("owner").equalTo(vendorId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    vendorProducts.clear()
                    for (productSnap in snapshot.children) {
                        val product = productSnap.getValue(Product::class.java)
                        product?.let { vendorProducts.add(it) }
                    }
                    productAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@VendorDashboardActivity, "Failed to load products", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addProduct() {
        val name = productName.text.toString().trim()
        val price = productPrice.text.toString().toDoubleOrNull()
        val description = productDescription.text.toString().trim()
        val imageUrl = productImage.text.toString().trim()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (name.isEmpty() || price == null || description.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val productId = databaseProducts.push().key ?: return
        val product = Product(productId, name, price, description, imageUrl, userId)

        databaseProducts.child(productId).setValue(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add product.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        productName.text.clear()
        productPrice.text.clear()
        productDescription.text.clear()
        productImage.text.clear()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}
