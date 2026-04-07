package com.example.heritagehub.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.heritagehub.model.Artwork

class ArtisanViewModel : ViewModel() {
    // Local state for artworks (temporary, will be replaced with Firestore later)
    val artworks = mutableStateOf<List<Artwork>>(
        listOf(
            Artwork(
                id = "1",
                title = "Moonlight Sonata",
                artistName = "Current Artisan",
                imageUrl = "https://via.placeholder.com/400",
                description = "A beautiful abstract piece inspired by Chopin's classical masterpiece",
                price = "$500",
                category = "Painting",
                customizationAvailable = true,
                videoUrl = null
            ),
            Artwork(
                id = "2",
                title = "Golden Hour",
                artistName = "Current Artisan",
                imageUrl = "https://via.placeholder.com/400",
                description = "Capturing the magical moments of sunset",
                price = "$750",
                category = "Photography",
                customizationAvailable = false,
                videoUrl = null
            ),
            Artwork(
                id = "3",
                title = "Serenity",
                artistName = "Current Artisan",
                imageUrl = "https://via.placeholder.com/400",
                description = "A minimalist representation of inner peace",
                price = "$400",
                category = "Digital Art",
                customizationAvailable = true,
                videoUrl = null
            ),
            Artwork(
                id = "4",
                title = "Echoes",
                artistName = "Current Artisan",
                imageUrl = "https://via.placeholder.com/400",
                description = "Modern sculpture exploring the concept of sound waves",
                price = "$1200",
                category = "Sculpture",
                customizationAvailable = false,
                videoUrl = null
            )
        )
    )

    fun addArtwork(
        title: String,
        artistName: String,
        imageUrl: String,
        description: String = "",
        price: String = "",
        category: String = "",
        customizationAvailable: Boolean = false,
        videoUrl: String? = null
    ) {
        if (title.isEmpty() || artistName.isEmpty() || imageUrl.isEmpty()) {
            return
        }

        val newArtwork = Artwork(
            id = System.currentTimeMillis().toString(),
            title = title,
            artistName = artistName,
            imageUrl = imageUrl,
            description = description,
            price = price,
            category = category,
            customizationAvailable = customizationAvailable,
            videoUrl = videoUrl
        )

        val currentList = artworks.value.toMutableList()
        currentList.add(0, newArtwork) // Add to beginning
        artworks.value = currentList
    }

    fun getArtworks(): List<Artwork> {
        return artworks.value
    }
}

