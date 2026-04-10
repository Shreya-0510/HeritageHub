package com.example.heritagehub.model

data class CartItem(
    val artworkId: String = "",
    val title: String = "",
    val artistName: String = "",
    val imageUrl: String = "",
    val artistId: String = "",
    val priceDisplay: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 1,
    val updatedAt: Long = 0L
) {
    val lineTotal: Double
        get() = unitPrice * quantity
}

