package com.example.heritagehub.data

import com.example.heritagehub.model.Artisan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ArtisanRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getArtisans(): List<Artisan> {
        val snapshot = firestore.collection("artworks").get().await()

        val grouped = snapshot.documents.groupBy { doc ->
            val artistId = doc.getString("artistId").orEmpty()
            val artistName = doc.getString("artistName").orEmpty()
            if (artistId.isNotBlank()) artistId else artistName
        }

        return grouped.values.mapNotNull { docs ->
            val first = docs.firstOrNull() ?: return@mapNotNull null
            val artistName = first.getString("artistName").orEmpty()
            if (artistName.isBlank()) return@mapNotNull null

            val categories = docs
                .mapNotNull { it.getString("category") }
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

            Artisan(
                artistId = first.getString("artistId").orEmpty(),
                artistName = artistName,
                categories = categories,
                artworkCount = docs.size
            )
        }.sortedBy { it.artistName.lowercase() }
    }
}

