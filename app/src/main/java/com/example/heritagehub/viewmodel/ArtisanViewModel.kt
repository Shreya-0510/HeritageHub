package com.example.heritagehub.viewmodel

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.heritagehub.model.Artisan
import com.example.heritagehub.model.Artwork
import com.example.heritagehub.model.CustomizationRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ArtisanViewModel : ViewModel() {
    val artworks = mutableStateOf<List<Artwork>>(emptyList())
    val requests = mutableStateOf<List<CustomizationRequest>>(emptyList())
    val isLoading = mutableStateOf(false)
    val currentArtisan = mutableStateOf<Artisan?>(null)

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun readStringList(raw: Any?): List<String> {
        return (raw as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    }

    init {
        refreshArtworks()
        refreshCustomizationRequests()
        refreshArtisanProfile()
    }

    fun refreshArtisanProfile() {
        val currentUserId = auth.currentUser?.uid ?: return
        
        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                if (documentExists(doc)) {
                    currentArtisan.value = Artisan(
                        artistId = doc.id,
                        artistName = doc.getString("username").orEmpty(),
                        profilePicUrl = doc.getString("profilePicUrl").orEmpty(),
                        description = doc.getString("description").orEmpty(),
                        skills = readStringList(doc.get("skills")),
                        location = doc.getString("location").orEmpty()
                    )
                }
            }
    }

    private fun documentExists(doc: com.google.firebase.firestore.DocumentSnapshot): Boolean = doc.exists()

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
                    val imageUrls = readStringList(doc.get("imageUrls")).ifEmpty {
                        doc.getString("imageUrl")?.takeIf { it.isNotBlank() }?.let { listOf(it) } ?: emptyList()
                    }
                    val videoUrls = readStringList(doc.get("videoUrls")).ifEmpty {
                        doc.getString("videoUrl")?.takeIf { it.isNotBlank() }?.let { listOf(it) } ?: emptyList()
                    }

                    Artwork(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        artistName = doc.getString("artistName") ?: "",
                        imageUrl = imageUrls.firstOrNull().orEmpty(),
                        imageUrls = imageUrls,
                        description = doc.getString("description") ?: "",
                        price = doc.getString("price") ?: "",
                        category = doc.getString("category") ?: "",
                        customizationAvailable = doc.getBoolean("customizationAvailable") ?: false,
                        videoUrl = videoUrls.firstOrNull(),
                        videoUrls = videoUrls,
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
                        userName = doc.getString("userName").orEmpty(),
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

    fun updateRequestStatus(requestId: String, newStatus: String) {
        firestore.collection("customization_requests").document(requestId)
            .update("status", newStatus)
            .addOnSuccessListener {
                refreshCustomizationRequests()
            }
    }

    suspend fun updateProfile(
        context: Context,
        profilePicUri: Uri?,
        description: String,
        skills: List<String>,
        location: String
    ): Result<Unit> {
        val currentUserId = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User is not logged in"))

        isLoading.value = true
        return try {
            val updateMap = mutableMapOf<String, Any>(
                "description" to description,
                "skills" to skills,
                "location" to location
            )

            if (profilePicUri != null && profilePicUri.scheme != "http" && profilePicUri.scheme != "https") {
                val localUri = copyUriToLocalFile(
                    context = context,
                    sourceUri = profilePicUri,
                    artistId = currentUserId,
                    artworkId = "profile",
                    folder = "images",
                    fallbackExtension = "jpg"
                )
                updateMap["profilePicUrl"] = localUri.toString()
            }

            firestore.collection("users").document(currentUserId)
                .update(updateMap).await()
            
            refreshArtisanProfile()
            isLoading.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            isLoading.value = false
            Result.failure(e)
        }
    }

    suspend fun addArtwork(
        context: Context,
        title: String,
        artistName: String,
        imageUris: List<Uri>,
        description: String,
        price: String,
        category: String,
        customizationAvailable: Boolean,
        videoUris: List<Uri> = emptyList()
    ): Result<Unit> {
        if (title.isBlank() || artistName.isBlank() || imageUris.isEmpty()) {
            return Result.failure(IllegalArgumentException("Missing required artwork fields"))
        }

        val currentUserId = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User is not logged in"))

        return try {
            val artworkRef = firestore.collection("artworks").document()
            val artworkId = artworkRef.id

            val uploadedImageUrls = uploadMediaFiles(
                context = context,
                artistId = currentUserId,
                artworkId = artworkId,
                mediaUris = imageUris,
                folder = "images",
                extension = "jpg"
            )
            val uploadedVideoUrls = uploadMediaFiles(
                context = context,
                artistId = currentUserId,
                artworkId = artworkId,
                mediaUris = videoUris,
                folder = "videos",
                extension = "mp4"
            )

            val artwork = mapOf(
                "title" to title,
                "artistName" to artistName,
                "imageUrl" to uploadedImageUrls.firstOrNull().orEmpty(),
                "imageUrls" to uploadedImageUrls,
                "description" to description,
                "price" to price,
                "category" to category,
                "customizationAvailable" to customizationAvailable,
                "videoUrl" to uploadedVideoUrls.firstOrNull(),
                "videoUrls" to uploadedVideoUrls,
                "artistId" to currentUserId,
                "createdAt" to System.currentTimeMillis()
            )

            artworkRef.set(artwork).await()
            refreshArtworks()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadMediaFiles(
        context: Context,
        artistId: String,
        artworkId: String,
        mediaUris: List<Uri>,
        folder: String,
        extension: String
    ): List<String> {
        if (mediaUris.isEmpty()) return emptyList()

        return mediaUris.map { uri ->
            copyUriToLocalFile(
                context = context,
                sourceUri = uri,
                artistId = artistId,
                artworkId = artworkId,
                folder = folder,
                fallbackExtension = extension
            ).toString()
        }
    }

    private fun copyUriToLocalFile(
        context: Context,
        sourceUri: Uri,
        artistId: String,
        artworkId: String,
        folder: String,
        fallbackExtension: String
    ): Uri {
        val extension = context.contentResolver.getType(sourceUri)
            ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
            ?.ifBlank { null }
            ?: fallbackExtension

        val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension"
        val baseDir = context.filesDir
            .resolve("artworks")
            .resolve(artistId)
            .resolve(artworkId)
            .resolve(folder)
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }

        val outputFile = baseDir.resolve(fileName)
        context.contentResolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "Unable to read selected media" }
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return Uri.fromFile(outputFile)
    }

    suspend fun updateArtwork(
        artwork: Artwork,
        title: String,
        description: String,
        price: String,
        category: String,
        customizationAvailable: Boolean,
        imageUrls: List<String>,
        videoUrls: List<String>
    ): Result<Unit> {
        val currentUserId = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User is not logged in"))
        return try {
            val artworkRef = firestore.collection("artworks").document(artwork.id)
            val updateMap = mapOf(
                "title" to title,
                "description" to description,
                "price" to price,
                "category" to category,
                "customizationAvailable" to customizationAvailable,
                "imageUrl" to imageUrls.firstOrNull().orEmpty(),
                "imageUrls" to imageUrls,
                "videoUrl" to videoUrls.firstOrNull(),
                "videoUrls" to videoUrls
            )
            artworkRef.update(updateMap).await()
            refreshArtworks()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
