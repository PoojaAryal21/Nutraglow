package com.example.nutraglow

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class CustomerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: MutableList<Product>
    private lateinit var filteredList: MutableList<Product>
    private lateinit var databaseReference: DatabaseReference

    private lateinit var logoutButton: Button
    private lateinit var goToCartButton: Button
    private lateinit var searchInput: EditText
    private lateinit var sortByPriceButton: Button
    private lateinit var sortByNameButton: Button
    private lateinit var emptyStateText: TextView

    private lateinit var navCart: Button
    private lateinit var navAccount: Button
    private lateinit var navHome: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)

        recyclerView = findViewById(R.id.recyclerViewProduct)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        productList = mutableListOf()
        filteredList = mutableListOf()

        productAdapter = ProductAdapter(
            productList = filteredList,
            isCart = false,
            showDescription = false,
            onItemClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra("productId", product.productId ?: "")
                    putExtra("name", product.name ?: "")
                    putExtra("price", product.price)
                    putExtra("description", product.description ?: "")
                    putExtra("imageUrl", product.imageUrl ?: "")
                }
                startActivity(intent)
            }
        )
        recyclerView.adapter = productAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("products")

        logoutButton = findViewById(R.id.logoutButton)
        goToCartButton = findViewById(R.id.goToCartButton)
        searchInput = findViewById(R.id.searchInput)
        sortByPriceButton = findViewById(R.id.sortByPriceButton)
        sortByNameButton = findViewById(R.id.sortByNameButton)
        emptyStateText = findViewById(R.id.emptyText)

        navCart = findViewById(R.id.navCart)
        navAccount = findViewById(R.id.navAccount)
        navHome = findViewById(R.id.navHome)

        logoutButton.setOnClickListener { logoutUser() }
        goToCartButton.setOnClickListener { startActivity(Intent(this, CartActivity::class.java)) }

        navCart.setOnClickListener { startActivity(Intent(this, CartActivity::class.java)) }
        navAccount.setOnClickListener { startActivity(Intent(this, AccountActivity::class.java)) }
        navHome.setOnClickListener { recreate() }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }
        })

        sortByPriceButton.setOnClickListener {
            filteredList.sortBy { it.price }
            productAdapter.notifyDataSetChanged()
        }

        sortByNameButton.setOnClickListener {
            filteredList.sortBy { it.name?.lowercase(Locale.ROOT) }
            productAdapter.notifyDataSetChanged()
        }

        fetchProducts()
    }

    private fun fetchProducts() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    if (product != null) productList.add(product)
                }
                filterProducts(searchInput.text.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CustomerActivity, "Failed to load products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterProducts(query: String) {
        val lowerQuery = query.lowercase(Locale.ROOT)
        filteredList.clear()
        filteredList.addAll(productList.filter {
            it.name?.lowercase(Locale.ROOT)?.contains(lowerQuery) == true ||
                    it.description?.lowercase(Locale.ROOT)?.contains(lowerQuery) == true
        })
        productAdapter.notifyDataSetChanged()
        emptyStateText.visibility = if (filteredList.isEmpty()) TextView.VISIBLE else TextView.GONE
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}
