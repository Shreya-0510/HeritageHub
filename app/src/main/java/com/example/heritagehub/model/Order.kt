package com.example.heritagehub.model

data class Order(
    val id: String = "",
    val status: String = "placed",
    val itemCount: Int = 0,
    val total: Double = 0.0,
    val createdAt: Long = 0L,
    val deliveringTo: String = "",
    val paymentMethod: String = "",
    val firstItemTitle: String = ""
)

