package com.example.heritagehub.model

data class Artisan(
    val artistId: String = "",
    val artistName: String = "",
    val categories: List<String> = emptyList(),
    val artworkCount: Int = 0
)

