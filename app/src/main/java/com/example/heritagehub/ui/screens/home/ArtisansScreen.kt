package com.example.heritagehub.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heritagehub.model.Artisan
import com.example.heritagehub.viewmodel.ArtisanDirectoryViewModel

@Composable
fun ArtisansScreen(
    onOpenProfile: (String) -> Unit
) {
    val viewModel: ArtisanDirectoryViewModel = viewModel()
    val artisans = viewModel.visibleArtisans.value
    val searchQuery = viewModel.searchQuery.value
    val isLoading = viewModel.isLoading.value

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            singleLine = true,
            label = { Text("Search by artist or category") },
            placeholder = { Text("e.g. Madhubani, pottery, woodwork") }
        )

        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            artisans.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No artisans found")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = artisans,
                        key = { index, artisan ->
                            "${artisan.artistId}-${artisan.artistName}-$index"
                        }
                    ) { _, artisan ->
                        ArtisanCard(artisan = artisan, onOpenProfile = onOpenProfile)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtisanCard(
    artisan: Artisan,
    onOpenProfile: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenProfile(artisan.artistName) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(artisan.artistName, fontWeight = FontWeight.Bold)
            Text(
                text = "${artisan.artworkCount} artwork(s)",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val categoryText = if (artisan.categories.isEmpty()) {
                    "Category not specified"
                } else {
                    artisan.categories.joinToString(", ")
                }
                Text(
                    text = categoryText,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}



