package com.example.nutraglow

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var productImage: ImageView
    private lateinit var productName: TextView
    private lateinit var productPrice: TextView
    private lateinit var productDescription: TextView
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var writeReviewButton: Button

    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var reviewList: MutableList<Review>

    private lateinit var databaseReviews: DatabaseReference

    private var productId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        productImage = findViewById(R.id.productDetailImage)
        productName = findViewById(R.id.productDetailName)
        productPrice = findViewById(R.id.productDetailPrice)
        productDescription = findViewById(R.id.productDetailDescription)
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)
        writeReviewButton = findViewById(R.id.writeReviewButton)

        // Load Product Info
        productId = intent.getStringExtra("productId") ?: ""
        productName.text = intent.getStringExtra("name")
        productPrice.text = "Rs. ${intent.getDoubleExtra("price", 0.0)}"
        productDescription.text = intent.getStringExtra("description")

        val imageUrl = intent.getStringExtra("imageUrl")
        Glide.with(this).load(imageUrl).into(productImage)

        // Setup Reviews
        reviewList = mutableListOf()
        reviewAdapter = ReviewAdapter(reviewList)
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewRecyclerView.adapter = reviewAdapter

        databaseReviews = FirebaseDatabase.getInstance().getReference("reviews").child(productId)

        fetchReviews()

        writeReviewButton.setOnClickListener {
            openReviewDialog()
        }
    }

    private fun fetchReviews() {
        databaseReviews.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reviewList.clear()
                for (reviewSnap in snapshot.children) {
                    val review = reviewSnap.getValue(Review::class.java)
                    review?.let { reviewList.add(it) }
                }
                reviewAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProductDetailActivity, "Failed to load reviews.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openReviewDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_write_review, null)

        val ratingBar = dialogLayout.findViewById<RatingBar>(R.id.ratingBar)
        val reviewText = dialogLayout.findViewById<EditText>(R.id.reviewComment)

        builder.setView(dialogLayout)
        builder.setTitle("Write a Review")

        builder.setPositiveButton("Submit") { dialog, _ ->
            val rating = ratingBar.rating.toInt()
            val comment = reviewText.text.toString().trim()
            saveReview(rating, comment)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun saveReview(rating: Int, comment: String) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(this, "Please sign in to leave a review.", Toast.LENGTH_SHORT).show()
            return
        }

        val userName = user.email ?: "Anonymous"
        val reviewId = databaseReviews.push().key ?: return
        val review = Review(userId = user.uid, userName = userName, rating = rating, comment = comment)

        databaseReviews.child(reviewId).setValue(review)
            .addOnSuccessListener {
                Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to submit review.", Toast.LENGTH_SHORT).show()
            }
    }
}
