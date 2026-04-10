package com.example.heritagehub.model

data class Artwork(
    val id: String,
    val title: String,
    val artistName: String,
    val imageUrl: String,
    val imageUrls: List<String> = emptyList(),
    val description: String = "",
    val price: String = "",
    val category: String = "",
    val customizationAvailable: Boolean = false,
    val videoUrl: String? = null,
    val videoUrls: List<String> = emptyList(),
    val artistId: String = ""
) {
    val allImageUrls: List<String>
        get() = if (imageUrls.isNotEmpty()) imageUrls else listOfNotNull(imageUrl.takeIf { it.isNotBlank() })

    val allVideoUrls: List<String>
        get() = if (videoUrls.isNotEmpty()) videoUrls else listOfNotNull(videoUrl?.takeIf { it.isNotBlank() })

    val primaryImageUrl: String
        get() = allImageUrls.firstOrNull().orEmpty()
}

