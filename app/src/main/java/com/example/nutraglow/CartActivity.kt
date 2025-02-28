package com.example.nutraglow

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartList: MutableList<Product>
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        recyclerView = findViewById(R.id.recyclerViewCart)
        recyclerView.layoutManager = LinearLayoutManager(this)

        cartList = mutableListOf()
        productAdapter = ProductAdapter(cartList, isCart = true) // ✅ Ensuring isCart = true
        recyclerView.adapter = productAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("cart")
        fetchCartItems()
    }

    private fun fetchCartItems() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartList.clear()
                for (cartSnapshot in snapshot.children) {
                    val product = cartSnapshot.getValue(Product::class.java)
                    if (product != null) {
                        cartList.add(product)
                    }
                }
                productAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CartActivity, "Failed to load cart: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
