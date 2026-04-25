package com.example.heritagehub.ui.screens.artisan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heritagehub.model.Artwork
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanArtworkDetailScreen(
    artwork: Artwork,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artwork Details") },
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
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image carousel (read-only)
            ImageCarouselReadOnly(imageUrls = artwork.allImageUrls, title = artwork.title)
            OutlinedTextField(
                value = artwork.title,
                onValueChange = {},
                label = { Text("Title") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = artwork.description,
                onValueChange = {},
                label = { Text("Description") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = artwork.category,
                onValueChange = {},
                label = { Text("Category") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = artwork.price,
                onValueChange = {},
                label = { Text("Price") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = artwork.customizationAvailable,
                    onCheckedChange = {},
                    enabled = false
                )
                Text("Customization Available")
            }
            // Videos
            if (artwork.allVideoUrls.isNotEmpty()) {
                Text("Videos:", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    artwork.allVideoUrls.forEach { url ->
                        OutlinedTextField(
                            value = url,
                            onValueChange = {},
                            label = { Text("Video URL") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Artwork")
            }
        }
    }
}

@Composable
private fun ImageCarouselReadOnly(imageUrls: List<String>, title: String) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (imageUrls.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .height(220.dp)
                        .width(180.dp)
//                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No image")
                }
            }
        } else {
            items(imageUrls) { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .height(220.dp)
                        .width(180.dp)
//                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }
    }
}
