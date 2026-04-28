package com.example.heritagehub.data

import com.example.heritagehub.model.Artwork
import com.example.heritagehub.util.FileUtil
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

        return snapshot.documents.mapNotNull { doc ->
            val rawImageUrls = readStringList(doc.get("imageUrls")).ifEmpty {
                doc.getString("imageUrl")?.takeIf { it.isNotBlank() }?.let { listOf(it) } ?: emptyList()
            }
            val rawVideoUrls = readStringList(doc.get("videoUrls")).ifEmpty {
                doc.getString("videoUrl")?.takeIf { it.isNotBlank() }?.let { listOf(it) } ?: emptyList()
            }

            // TEMPORARY GUARD: Filter URIs that don't exist on this device
            val validImageUrls = FileUtil.filterValidUris(rawImageUrls)
            val validVideoUrls = FileUtil.filterValidUris(rawVideoUrls)

            // If no images are valid for this artwork on this device, skip rendering it to avoid broken UI
            if (validImageUrls.isEmpty()) return@mapNotNull null

            Artwork(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                artistName = doc.getString("artistName").orEmpty(),
                imageUrl = validImageUrls.firstOrNull().orEmpty(),
                imageUrls = validImageUrls,
                description = doc.getString("description").orEmpty(),
                price = doc.getString("price").orEmpty(),
                category = doc.getString("category").orEmpty(),
                customizationAvailable = doc.getBoolean("customizationAvailable") ?: false,
                videoUrl = validVideoUrls.firstOrNull(),
                videoUrls = validVideoUrls,
                artistId = doc.getString("artistId").orEmpty()
            )
        }.reversed()
    }
}
