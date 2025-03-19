package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class VendorActivity : AppCompatActivity() {

    private lateinit var databaseProducts: DatabaseReference
    private lateinit var productName: EditText
    private lateinit var productPrice: EditText
    private lateinit var productDescription: EditText
    private lateinit var productImage: EditText
    private lateinit var addProductButton: Button
    private lateinit var logoutButton: Button  // ✅ Added Logout Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor)

        databaseProducts = FirebaseDatabase.getInstance().getReference("products")

        productName = findViewById(R.id.productName)
        productPrice = findViewById(R.id.productPrice)
        productDescription = findViewById(R.id.productDescription)
        productImage = findViewById(R.id.productImage)
        addProductButton = findViewById(R.id.addProductButton)
        logoutButton = findViewById(R.id.logoutButton) // ✅ Initialize Logout Button

        addProductButton.setOnClickListener {
            addProduct()
        }

        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun addProduct() {
        val name = productName.text.toString()
        val price = productPrice.text.toString().toDoubleOrNull()
        val description = productDescription.text.toString()
        val imageUrl = productImage.text.toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (name.isEmpty() || price == null || description.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val productId = databaseProducts.push().key!!
        val product = Product(productId, name, price, description, imageUrl, userId)

        databaseProducts.child(productId).setValue(product).addOnSuccessListener {
            Toast.makeText(this, "Product Added!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // ✅ Logout Functionality
    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()  // Logs out user
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Redirect to SignInActivity
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish() // Close VendorActivity
    }
}
