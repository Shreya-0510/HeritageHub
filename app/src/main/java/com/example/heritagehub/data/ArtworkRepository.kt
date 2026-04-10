package com.example.heritagehub.data

import com.example.heritagehub.model.Artwork
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ArtworkRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getArtworks(): List<Artwork> {
        val snapshot = firestore.collection("artworks")
            .orderBy("createdAt")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            Artwork(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                artistName = doc.getString("artistName").orEmpty(),
                imageUrl = doc.getString("imageUrl").orEmpty(),
                description = doc.getString("description").orEmpty(),
                price = doc.getString("price").orEmpty(),
                category = doc.getString("category").orEmpty(),
                customizationAvailable = doc.getBoolean("customizationAvailable") ?: false,
                videoUrl = doc.getString("videoUrl"),
                artistId = doc.getString("artistId").orEmpty()
            )
        }.reversed()
    }
}

