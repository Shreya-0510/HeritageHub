package com.example.heritagehub.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.heritagehub.model.Artwork
import com.example.heritagehub.model.CustomizationRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ArtisanViewModel : ViewModel() {
    val artworks = mutableStateOf<List<Artwork>>(emptyList())
    val requests = mutableStateOf<List<CustomizationRequest>>(emptyList())
    val isLoading = mutableStateOf(false)

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        refreshArtworks()
        refreshCustomizationRequests()
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
                        videoUrl = doc.getString("videoUrl"),
                        artistId = doc.getString("artistId") ?: ""
                    )
                }
                artworks.value = artworkList
                isLoading.value = false
            }
            .addOnFailureListener {
                isLoading.value = false
            }
    }

    fun refreshCustomizationRequests() {
        val currentUserId = auth.currentUser?.uid ?: run {
            requests.value = emptyList()
            return
        }

        firestore.collection("customization_requests")
            .whereEqualTo("artistId", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                requests.value = snapshot.documents.map { doc ->
                    CustomizationRequest(
                        id = doc.id,
                        artistId = doc.getString("artistId").orEmpty(),
                        artistName = doc.getString("artistName").orEmpty(),
                        userId = doc.getString("userId").orEmpty(),
                        description = doc.getString("description").orEmpty(),
                        budget = doc.getString("budget").orEmpty(),
                        deadline = doc.getString("deadline").orEmpty(),
                        status = doc.getString("status") ?: "pending"
                    )
                }
            }
            .addOnFailureListener {
                requests.value = emptyList()
            }
    }

    suspend fun addArtwork(
        title: String,
        artistName: String,
        imageUrl: String,
        description: String,
        price: String,
        category: String,
        customizationAvailable: Boolean,
        videoUrl: String? = null
    ): Result<Unit> {
        if (title.isBlank() || artistName.isBlank() || imageUrl.isBlank()) {
            return Result.failure(IllegalArgumentException("Missing required artwork fields"))
        }

        val currentUserId = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User is not logged in"))

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

        return try {
            firestore.collection("artworks").add(artwork).await()
            refreshArtworks()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getArtworks(): List<Artwork> {
        return artworks.value
    }
}


