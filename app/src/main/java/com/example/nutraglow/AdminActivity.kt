package com.example.nutraglow

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var productNameField: EditText
    private lateinit var productPriceField: EditText
    private lateinit var productDescriptionField: EditText
    private lateinit var productImageUrlField: EditText
    private lateinit var addProductButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference.child("products")

        // Initialize UI elements (Ensure these IDs exist in `activity_admin.xml`)
        productNameField = findViewById(R.id.productNameField)
        productPriceField = findViewById(R.id.productPriceField)
        productDescriptionField = findViewById(R.id.productDescriptionField)
        productImageUrlField = findViewById(R.id.productImageUrlField)
        addProductButton = findViewById(R.id.addProductButton)

        // Set click listener for the add product button
        addProductButton.setOnClickListener {
            addProductToDatabase()
        }
    }

    private fun addProductToDatabase() {
        val name = productNameField.text.toString().trim()
        val price = productPriceField.text.toString().trim() // Keep as String
        val description = productDescriptionField.text.toString().trim()
        val imageUrl = productImageUrlField.text.toString().trim()

        // Validate inputs
        if (name.isEmpty() || price.isEmpty() || description.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate unique product ID
        val productId = database.push().key ?: return

        // Create Product object (Now matches your Product class)
        val product = Product(productId, name, price, description, imageUrl)

        // Push to Firebase Database
        database.child(productId).setValue(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add product: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun clearFields() {
        productNameField.text.clear()
        productPriceField.text.clear()
        productDescriptionField.text.clear()
        productImageUrlField.text.clear()
    }
}
