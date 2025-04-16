package com.example.nutraglow

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ProductDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val image = findViewById<ImageView>(R.id.productDetailImage)
        val name = findViewById<TextView>(R.id.productDetailName)
        val price = findViewById<TextView>(R.id.productDetailPrice)
        val description = findViewById<TextView>(R.id.productDetailDescription)

        name.text = intent.getStringExtra("name")
        price.text = "Rs. ${intent.getDoubleExtra("price", 0.0)}"
        description.text = intent.getStringExtra("description")

        val imageUrl = intent.getStringExtra("imageUrl")
        Glide.with(this).load(imageUrl).into(image)
    }
}
