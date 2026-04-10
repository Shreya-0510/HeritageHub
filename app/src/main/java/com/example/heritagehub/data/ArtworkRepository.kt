package com.example.heritagehub.data

import com.example.heritagehub.model.Artwork
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ArtworkRepository {
    private val firestore = FirebaseFirestore.getInstance()

    private fun readStringList(raw: Any?): List<String> {
        return (raw as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    }

    suspend fun getArtworks(): List<Artwork> {
        val snapshot = firestore.collection("artworks")
            .orderBy("createdAt")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            val imageUrls = readStringList(doc.get("imageUrls")).ifEmpty {
                doc.getString("imageUrl")?.takeIf { it.isNotBlank() }?.let { listOf(it) } ?: emptyList()
            }
            val videoUrls = readStringList(doc.get("videoUrls")).ifEmpty {
                doc.getString("videoUrl")?.takeIf { it.isNotBlank() }?.let { listOf(it) } ?: emptyList()
            }

            Artwork(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                artistName = doc.getString("artistName").orEmpty(),
                imageUrl = imageUrls.firstOrNull().orEmpty(),
                imageUrls = imageUrls,
                description = doc.getString("description").orEmpty(),
                price = doc.getString("price").orEmpty(),
                category = doc.getString("category").orEmpty(),
                customizationAvailable = doc.getBoolean("customizationAvailable") ?: false,
                videoUrl = videoUrls.firstOrNull(),
                videoUrls = videoUrls,
                artistId = doc.getString("artistId").orEmpty()
            )
        }.reversed()
    }
}

