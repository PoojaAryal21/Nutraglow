package com.example.nutraglow

data class OrderStatus(
    val status: String = "",
    val timestamp: Long = System.currentTimeMillis()
)