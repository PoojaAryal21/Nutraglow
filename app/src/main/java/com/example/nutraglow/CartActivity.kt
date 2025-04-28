package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartList: MutableList<Product>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var logoutButton: Button
    private lateinit var proceedToPayButton: Button
    private lateinit var totalAmountText: TextView
    private lateinit var navHome: Button
    private lateinit var navCart: Button
    private lateinit var navAccount: Button

    private var userId: String = "guest"
    private val cartKeyMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        userId = firebaseUser?.uid ?: "guest"

        recyclerView = findViewById(R.id.recyclerViewCart)
        recyclerView.layoutManager = LinearLayoutManager(this)

        totalAmountText = findViewById(R.id.totalAmountText)
        proceedToPayButton = findViewById(R.id.proceedToPayButton)

        navHome = findViewById(R.id.navHome)
        navCart = findViewById(R.id.navCart)
        navAccount = findViewById(R.id.navAccount)

        navHome.setOnClickListener {
            val intent = Intent(this, CustomerActivity::class.java)
            intent.putExtra("isGuest", userId == "guest")
            startActivity(intent)
        }

        navCart.setOnClickListener { recreate() }

        navAccount.setOnClickListener {
            if (userId == "guest") {
                Toast.makeText(this, "Please sign in first.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SignInActivity::class.java))
            } else {
                startActivity(Intent(this, AccountActivity::class.java))
            }
        }

        cartList = mutableListOf()
        productAdapter = ProductAdapter(cartList, isCart = true, userId = userId)
        recyclerView.adapter = productAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("carts").child(userId).child("products")

        fetchCartItems()

        logoutButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener { logoutUser() }

        proceedToPayButton.setOnClickListener {
            if (userId == "guest") {
                Toast.makeText(this, "You need to Sign In to checkout.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SignInActivity::class.java))
            } else {
                val total = cartList.sumOf { (it.quantity ?: 1) * (it.price ?: 0.0) }
                val totalItems = cartList.sumOf { it.quantity ?: 1 }

                val intent = Intent(this, CheckoutActivity::class.java).apply {
                    putExtra("TOTAL_AMOUNT", total)
                    putExtra("TOTAL_ITEMS", totalItems)
                }
                startActivity(intent)
            }
        }
    }

    private fun fetchCartItems() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartList.clear()
                cartKeyMap.clear()

                for (cartSnapshot in snapshot.children) {
                    val product = cartSnapshot.getValue(Product::class.java)
                    val firebaseKey = cartSnapshot.key
                    if (product != null && firebaseKey != null) {
                        cartList.add(product)
                        cartKeyMap[product.productId!!] = firebaseKey
                    }
                }

                productAdapter.updateCartKeys(cartKeyMap)
                productAdapter.notifyDataSetChanged()
                updateTotalSummary()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CartActivity, "Failed to load cart: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTotalSummary() {
        val totalAmount = cartList.sumOf { (it.quantity ?: 1) * (it.price ?: 0.0) }
        val totalItems = cartList.sumOf { it.quantity ?: 1 }
        totalAmountText.text = "Total: Rs. $totalAmount ($totalItems items)"
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}
