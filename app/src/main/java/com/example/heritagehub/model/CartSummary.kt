package com.example.heritagehub.model

data class CartSummary(
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val itemCount: Int = 0
)

