@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.screens.artisan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heritagehub.viewmodel.ArtisanViewModel
import com.example.heritagehub.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddArtworkScreen(
    viewModel: ArtisanViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onArtworkAdded: () -> Unit
) {
    // Get the real username from auth
    val artistName = authViewModel.userName.value ?: "Artisan"

    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }
    val imageUrl = remember { mutableStateOf("") }
    val videoUrl = remember { mutableStateOf("") }
    val customizationAvailable = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isSubmitting = remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add New Artwork",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ========== BASIC INFORMATION SECTION ==========
            SectionHeader(title = "Basic Information")

            // Title Field
            OutlinedTextField(
                value = title.value,
                onValueChange = {
                    title.value = it
                    error.value = null
                },
                label = { Text("Artwork Title *") },
                placeholder = { Text("e.g., Moonlight Sonata") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                isError = error.value != null && title.value.isEmpty()
            )

            // Artist Name (Read-only)
            OutlinedTextField(
                value = artistName,
                onValueChange = { },
                label = { Text("Artist Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                enabled = false
            )

            // ========== DESCRIPTION SECTION ==========
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "Description & Details")

            // Description Field
            OutlinedTextField(
                value = description.value,
                onValueChange = {
                    description.value = it
                    error.value = null
                },
                label = { Text("Description *") },
                placeholder = { Text("Tell the story of your artwork...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = MaterialTheme.shapes.medium,
                maxLines = 4,
                isError = error.value != null && description.value.isEmpty()
            )

            // Category Field
            OutlinedTextField(
                value = category.value,
                onValueChange = {
                    category.value = it
                    error.value = null
                },
                label = { Text("Category *") },
                placeholder = { Text("e.g., Painting, Sculpture, Digital") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                isError = error.value != null && category.value.isEmpty()
            )

            // ========== MEDIA SECTION ==========
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "Media")

            // Image URL Field
            OutlinedTextField(
                value = imageUrl.value,
                onValueChange = {
                    imageUrl.value = it
                    error.value = null
                },
                label = { Text("Image URL *") },
                placeholder = { Text("https://example.com/image.jpg") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                isError = error.value != null && imageUrl.value.isEmpty()
            )

            // Video URL Field (Optional)
            OutlinedTextField(
                value = videoUrl.value,
                onValueChange = { videoUrl.value = it },
                label = { Text("Video URL (Optional)") },
                placeholder = { Text("https://example.com/video.mp4") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            // ========== PRICING & OPTIONS SECTION ==========
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "Pricing & Availability")

            // Price Field
            OutlinedTextField(
                value = price.value,
                onValueChange = {
                    price.value = it
                    error.value = null
                },
                label = { Text("Price *") },
                placeholder = { Text("e.g., $500, $1,000") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                isError = error.value != null && price.value.isEmpty()
            )

            // Customization Toggle
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Allow Customization",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Clients can request custom versions",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Checkbox(
                        checked = customizationAvailable.value,
                        onCheckedChange = { customizationAvailable.value = it }
                    )
                }
            }

            // ========== ERROR MESSAGE ==========
            if (error.value != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = error.value ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ========== ACTION BUTTONS ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel Button
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Submit Button
                Button(
                    onClick = {
                        when {
                            title.value.isEmpty() -> {
                                error.value = "Please enter artwork title"
                            }
                            description.value.isEmpty() -> {
                                error.value = "Please enter description"
                            }
                            category.value.isEmpty() -> {
                                error.value = "Please select a category"
                            }
                            imageUrl.value.isEmpty() -> {
                                error.value = "Please enter image URL"
                            }
                            price.value.isEmpty() -> {
                                error.value = "Please enter price"
                            }
                            else -> {
                                isSubmitting.value = true
                                scope.launch {
                                    try {
                                        val userId = auth.currentUser?.uid ?: return@launch

                                        val artwork = mapOf(
                                            "title" to title.value,
                                            "artistName" to artistName,
                                            "artistId" to userId,
                                            "imageUrl" to imageUrl.value,
                                            "description" to description.value,
                                            "price" to price.value,
                                            "category" to category.value,
                                            "customizationAvailable" to customizationAvailable.value,
                                            "videoUrl" to videoUrl.value.takeIf { it.isNotEmpty() },
                                            "createdAt" to System.currentTimeMillis()
                                        )

                                        firestore.collection("artworks")
                                            .add(artwork)
                                            .await()

                                        snackbarHostState.showSnackbar(
                                            message = "Artwork added successfully!",
                                            duration = SnackbarDuration.Short
                                        )

                                        isSubmitting.value = false
                                        // Also add to local viewmodel
                                        viewModel.addArtwork(
                                            title = title.value,
                                            artistName = artistName,
                                            imageUrl = imageUrl.value,
                                            description = description.value,
                                            price = price.value,
                                            category = category.value,
                                            customizationAvailable = customizationAvailable.value,
                                            videoUrl = videoUrl.value.takeIf { it.isNotEmpty() }
                                        )

                                        kotlinx.coroutines.delay(500)
                                        onArtworkAdded()
                                    } catch (e: Exception) {
                                        isSubmitting.value = false
                                        error.value = "Error: ${e.message ?: "Failed to add artwork"}"
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isSubmitting.value,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isSubmitting.value) "Adding..." else "Add Artwork",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}
