package com.example.nutraglow

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val customerName: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val paymentMethod: String = "",
    val totalAmount: Double = 0.0,
    val totalItems: Int = 0,
    var status: String = "Pending",
    val statusHistory: List<OrderStatus>? = null // ← updated
)
