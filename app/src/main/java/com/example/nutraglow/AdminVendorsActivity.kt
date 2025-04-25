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

class AdminVendorsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var vendorAdapter: VendorAdapter
    private val vendorList = mutableListOf<Pair<String, String>>() // Pair<userId, email>
    private val filteredList = mutableListOf<Pair<String, String>>()
    private var selectedVendorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_vendors)

        database = FirebaseDatabase.getInstance().getReference("users")

        val recyclerView = findViewById<RecyclerView>(R.id.vendorRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@AdminVendorsActivity)
            vendorAdapter = VendorAdapter(filteredList) { index ->
                selectedVendorId = filteredList.getOrNull(index)?.first
            }
            adapter = vendorAdapter
        }

        findViewById<EditText>(R.id.searchVendor).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = filterVendors(s.toString())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        findViewById<Button>(R.id.removeVendorButton).setOnClickListener {
            if (selectedVendorId != null) {
                AlertDialog.Builder(this)
                    .setTitle("Remove Vendor")
                    .setMessage("Are you sure you want to delete this vendor?")
                    .setPositiveButton("Yes") { _, _ -> removeVendor(selectedVendorId!!) }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(this, "Please select a vendor.", Toast.LENGTH_SHORT).show()
            }
        }

        loadVendorsFromFirebase()
    }

    private fun loadVendorsFromFirebase() {
        database.orderByChild("role").equalTo("vendor")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    vendorList.clear()
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key ?: continue
                        val email = userSnapshot.child("email").getValue(String::class.java)
                        if (!email.isNullOrEmpty()) vendorList.add(userId to email)
                    }
                    filterVendors("")
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AdminVendorsActivity, "Failed to load vendors.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filterVendors(query: String) {
        val lower = query.lowercase()
        filteredList.clear()
        filteredList.addAll(vendorList.filter { it.second.lowercase().contains(lower) })
        vendorAdapter.updateList(filteredList)
    }

    private fun removeVendor(userId: String) {
        database.child(userId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Vendor removed.", Toast.LENGTH_SHORT).show()
                selectedVendorId = null
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to remove vendor.", Toast.LENGTH_SHORT).show()
            }
    }
}
