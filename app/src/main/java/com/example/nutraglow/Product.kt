package com.example.nutraglow

data class Product(
    val productId: String? = null,
    val name: String? = null,
    val price: Double = 0.0,
    val description: String? = null,
    val imageUrl: String? = null,
    val owner: String? = null, // Added to track who added the product
    var quantity: Int = 1
)
