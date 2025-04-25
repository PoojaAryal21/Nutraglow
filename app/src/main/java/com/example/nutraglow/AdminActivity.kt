// Updated AdminActivity.kt with navigation buttons and structure
package com.example.nutraglow

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AdminActivity : AppCompatActivity() {

    private lateinit var databaseProducts: DatabaseReference
    private lateinit var storageReference: FirebaseStorage

    private lateinit var productNameInput: EditText
    private lateinit var productPriceInput: EditText
    private lateinit var productDescriptionInput: EditText
    private lateinit var productImageUrlInput: EditText
    private lateinit var selectImageButton: Button
    private lateinit var addProductButton: Button
    private lateinit var logoutButton: Button
    private lateinit var previewImageView: ImageView

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        databaseProducts = FirebaseDatabase.getInstance().getReference("products")
        storageReference = FirebaseStorage.getInstance()

        productNameInput = findViewById(R.id.productName)
        productPriceInput = findViewById(R.id.productPrice)
        productDescriptionInput = findViewById(R.id.productDescription)
        productImageUrlInput = findViewById(R.id.productImageUrl)
        selectImageButton = findViewById(R.id.selectImageButton)
        addProductButton = findViewById(R.id.addProductButton)
        logoutButton = findViewById(R.id.logoutButton)
        previewImageView = findViewById(R.id.productImagePreview)

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        addProductButton.setOnClickListener {
            addProductToDatabase()
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.viewOrdersButton).setOnClickListener {
            startActivity(Intent(this, AdminOrdersActivity::class.java))
        }

        findViewById<Button>(R.id.viewCustomersButton).setOnClickListener {
            startActivity(Intent(this, AdminCustomersActivity::class.java))
        }

        findViewById<Button>(R.id.viewVendorsButton).setOnClickListener {
            startActivity(Intent(this, AdminVendorsActivity::class.java))
        }

        findViewById<Button>(R.id.viewPaymentsButton).setOnClickListener {
            startActivity(Intent(this, AdminPaymentsActivity::class.java))
        }
    }

    private fun addProductToDatabase() {
        val name = productNameInput.text.toString().trim()
        val price = productPriceInput.text.toString().toDoubleOrNull() ?: 0.0
        val description = productDescriptionInput.text.toString().trim()
        val imageUrl = productImageUrlInput.text.toString().trim()

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        val productId = databaseProducts.push().key ?: return
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

        if (imageUrl.isNotEmpty()) {
            saveProductToDatabase(productId, name, price, description, imageUrl, currentUser)
        } else if (imageUri != null) {
            val imageRef = storageReference.reference.child("product_images/$productId.jpg")
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveProductToDatabase(productId, name, price, description, uri.toString(), currentUser)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please provide an image URL or select an image!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProductToDatabase(
        productId: String,
        name: String,
        price: Double,
        description: String,
        imageUrl: String,
        owner: String
    ) {
        val product = Product(productId, name, price, description, imageUrl, owner)
        databaseProducts.child(productId).setValue(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        productNameInput.text.clear()
        productPriceInput.text.clear()
        productDescriptionInput.text.clear()
        productImageUrlInput.text.clear()
        imageUri = null
        previewImageView.setImageResource(0)
        selectImageButton.text = "Select Image"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            selectImageButton.text = "Image Selected"
            previewImageView.setImageURI(imageUri)
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
