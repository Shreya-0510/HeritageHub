package com.example.heritagehub.ui.screens.artisan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heritagehub.model.Artwork
import com.example.heritagehub.viewmodel.ArtisanViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditArtworkScreen(
    artwork: Artwork,
    viewModel: ArtisanViewModel,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf(artwork.title) }
    var description by remember { mutableStateOf(artwork.description) }
    var price by remember { mutableStateOf(artwork.price) }
    var category by remember { mutableStateOf(artwork.category) }
    var customizationAvailable by remember { mutableStateOf(artwork.customizationAvailable) }
    var imageUrls by remember { mutableStateOf(artwork.allImageUrls.toMutableList()) }
    var videoUrls by remember { mutableStateOf(artwork.allVideoUrls.toMutableList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Media pickers (for simplicity, just add new URLs; real impl would upload and get URLs)
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            imageUrls = (imageUrls + uris.map { it.toString() }).toMutableList()
        }
    }
    val videoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            videoUrls = (videoUrls + uris.map { it.toString() }).toMutableList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Artwork") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = customizationAvailable,
                    onCheckedChange = { customizationAvailable = it }
                )
                Text("Customization Available")
            }
            Text("Images: ${imageUrls.size}", fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { imagePickerLauncher.launch("image/*") }) { Text("Add Images") }
                if (imageUrls.isNotEmpty()) {
                    Button(onClick = { imageUrls = mutableListOf() }) { Text("Clear Images") }
                }
            }
            Text("Videos: ${videoUrls.size}", fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { videoPickerLauncher.launch("video/*") }) { Text("Add Videos") }
                if (videoUrls.isNotEmpty()) {
                    Button(onClick = { videoUrls = mutableListOf() }) { Text("Clear Videos") }
                }
            }
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    isSaving = true
                    coroutineScope.launch {
                        val result = viewModel.updateArtwork(
                            artwork = artwork,
                            title = title,
                            description = description,
                            price = price,
                            category = category,
                            customizationAvailable = customizationAvailable,
                            imageUrls = imageUrls,
                            videoUrls = videoUrls
                        )
                        isSaving = false
                        if (result.isSuccess) {
                            onSave()
                        } else {
                            error = result.exceptionOrNull()?.message ?: "Failed to update artwork"
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSaving) "Saving..." else "Save Changes")
            }
        }
    }
}
