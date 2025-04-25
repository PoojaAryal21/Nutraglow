package com.example.nutraglow

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AdminCustomersActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var customerAdapter: CustomerAdapter
    private lateinit var customerList: MutableList<Pair<String, String>>
    private lateinit var filteredList: MutableList<Pair<String, String>>
    private var selectedCustomerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_customers)

        val searchBar = findViewById<EditText>(R.id.searchCustomer)
        val recyclerView = findViewById<RecyclerView>(R.id.customerRecyclerView)
        val removeButton = findViewById<Button>(R.id.removeCustomerButton)

        recyclerView.layoutManager = LinearLayoutManager(this)

        database = FirebaseDatabase.getInstance().getReference("users")
        customerList = mutableListOf()
        filteredList = mutableListOf()

        customerAdapter = CustomerAdapter(filteredList) { selectedIndex ->
            selectedCustomerId = filteredList.getOrNull(selectedIndex)?.first
        }

        recyclerView.adapter = customerAdapter

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = filterCustomers(s.toString())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        removeButton.setOnClickListener {
            selectedCustomerId?.let { userId ->
                AlertDialog.Builder(this)
                    .setTitle("Remove Customer")
                    .setMessage("Are you sure you want to delete this customer?")
                    .setPositiveButton("Yes") { _, _ ->
                        database.child(userId).removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Customer removed.", Toast.LENGTH_SHORT).show()
                                selectedCustomerId = null
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to remove customer.", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } ?: Toast.makeText(this, "Please select a customer.", Toast.LENGTH_SHORT).show()
        }

        loadCustomersFromFirebase()
    }

    private fun loadCustomersFromFirebase() {
        database.orderByChild("role").equalTo("customer")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    customerList.clear()
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key ?: continue
                        val email = userSnapshot.child("email").getValue(String::class.java)
                        email?.let {
                            customerList.add(userId to it)
                        }
                    }
                    filterCustomers("")
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AdminCustomersActivity, "Failed to load customers.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filterCustomers(query: String) {
        val lowerQuery = query.lowercase()
        filteredList.clear()
        filteredList.addAll(customerList.filter { it.second.lowercase().contains(lowerQuery) })
        customerAdapter.updateList(filteredList)
    }
}
