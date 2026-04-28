package com.example.heritagehub.data

import com.example.heritagehub.model.Artisan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ArtisanRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun readStringList(raw: Any?): List<String> {
        return (raw as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    }

    suspend fun getArtisans(): List<Artisan> {
        // Fetch all users with role 'artisan'
        val userSnapshot = firestore.collection("users")
            .whereEqualTo("role", "artisan")
            .get()
            .await()

        // Fetch all artworks to calculate counts
        val artworkSnapshot = firestore.collection("artworks").get().await()
        val artworkCounts = artworkSnapshot.documents
            .groupBy { it.getString("artistId") ?: "" }
            .mapValues { it.value.size }

        return userSnapshot.documents.map { doc ->
            val artisanId = doc.id
            val skills = readStringList(doc.get("skills"))
            
            Artisan(
                artistId = artisanId,
                artistName = doc.getString("username").orEmpty(),
                profilePicUrl = doc.getString("profilePicUrl").orEmpty(),
                description = doc.getString("description").orEmpty(),
                skills = skills,
                categories = skills, // Using skills as categories for search if needed
                artworkCount = artworkCounts[artisanId] ?: 0,
                location = doc.getString("location").orEmpty()
            )
        }.sortedBy { it.artistName.lowercase() }
    }
}
