package com.example.heritagehub.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.heritagehub.model.Artwork
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ArtisanViewModel : ViewModel() {
    // Local state for artworks (cached)
    val artworks = mutableStateOf<List<Artwork>>(emptyList())
    val isLoading = mutableStateOf(false)

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        refreshArtworks()
    }

    fun refreshArtworks() {
        isLoading.value = true
        val currentUserId = auth.currentUser?.uid ?: run {
            isLoading.value = false
            return
        }

        firestore.collection("artworks")
            .whereEqualTo("artistId", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                val artworkList = snapshot.documents.mapNotNull { doc ->
                    Artwork(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        artistName = doc.getString("artistName") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        description = doc.getString("description") ?: "",
                        price = doc.getString("price") ?: "",
                        category = doc.getString("category") ?: "",
                        customizationAvailable = doc.getBoolean("customizationAvailable") ?: false,
                        videoUrl = doc.getString("videoUrl")
                    )
                }
                artworks.value = artworkList
                isLoading.value = false
            }
            .addOnFailureListener {
                isLoading.value = false
            }
    }

    fun addArtwork(
        title: String,
        artistName: String,
        imageUrl: String,
        description: String,
        price: String,
        category: String,
        customizationAvailable: Boolean,
        videoUrl: String? = null
    ) {
        if (title.isEmpty() || artistName.isEmpty() || imageUrl.isEmpty()) {
            return
        }

        val currentUserId = auth.currentUser?.uid ?: return
        val artwork = mapOf(
            "title" to title,
            "artistName" to artistName,
            "imageUrl" to imageUrl,
            "description" to description,
            "price" to price,
            "category" to category,
            "customizationAvailable" to customizationAvailable,
            "videoUrl" to videoUrl,
            "artistId" to currentUserId,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("artworks")
            .add(artwork)
            .addOnSuccessListener {
                refreshArtworks()
            }
    }

    fun getArtworks(): List<Artwork> {
        return artworks.value
    }
}

