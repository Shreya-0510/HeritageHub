package com.example.heritagehub.model

@Suppress("unused")
data class CustomizationRequest(
    val id: String = "",
    val artistId: String = "",
    val artistName: String,
    val userId: String = "",
    val userName: String = "", // Added requester's name
    val description: String,
    val budget: String,
    val deadline: String,
    val status: String = "pending"
)
