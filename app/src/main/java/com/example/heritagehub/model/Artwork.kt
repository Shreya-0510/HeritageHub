package com.example.heritagehub.model

data class Artwork(
    val id: String,
    val title: String,
    val artistName: String,
    val imageUrl: String,
    val description: String = "",
    val price: String = "",
    val category: String = "",
    val customizationAvailable: Boolean = false,
    val videoUrl: String? = null
)

