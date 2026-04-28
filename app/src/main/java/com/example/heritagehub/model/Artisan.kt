package com.example.heritagehub.model

data class Artisan(
    val artistId: String = "",
    val artistName: String = "",
    val profilePicUrl: String = "",
    val description: String = "",
    val skills: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val artworkCount: Int = 0,
    val location: String = ""
)
