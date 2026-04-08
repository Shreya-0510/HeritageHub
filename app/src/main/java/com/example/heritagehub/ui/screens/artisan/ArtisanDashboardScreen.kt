@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.screens.artisan

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heritagehub.model.Artwork
import com.example.heritagehub.ui.components.ArtworkCard
import com.example.heritagehub.ui.components.ShimmerArtworkList
import com.example.heritagehub.ui.components.ShimmerRequestList
import com.example.heritagehub.viewmodel.AuthViewModel
import com.example.heritagehub.viewmodel.ArtisanViewModel

// ...existing code...
private fun getDummyArtworks(): List<Artwork> {
    return listOf(
        Artwork(
            id = "1",
            title = "Moonlight Sonata",
            artistName = "Artisan",
            imageUrl = "https://via.placeholder.com/400",
            description = "A beautiful abstract piece inspired by Chopin's classical masterpiece",
            price = "$500",
            category = "Painting",
            customizationAvailable = true
        ),
        Artwork(
            id = "2",
            title = "Golden Hour",
            artistName = "Artisan",
            imageUrl = "https://via.placeholder.com/400",
            description = "Capturing the magical moments of sunset",
            price = "$750",
            category = "Photography",
            customizationAvailable = false
        ),
        Artwork(
            id = "3",
            title = "Serenity",
            artistName = "Artisan",
            imageUrl = "https://via.placeholder.com/400",
            description = "A minimalist representation of inner peace",
            price = "$400",
            category = "Digital Art",
            customizationAvailable = true
        ),
        Artwork(
            id = "4",
            title = "Echoes",
            artistName = "Artisan",
            imageUrl = "https://via.placeholder.com/400",
            description = "Modern sculpture exploring the concept of sound waves",
            price = "$1200",
            category = "Sculpture",
            customizationAvailable = false
        )
    )
}

private fun getDummyRequests(): List<CustomizationRequestItem> {
    return listOf(
        CustomizationRequestItem(
            id = "r1",
            clientName = "John Doe",
            description = "Custom portrait painting",
            budget = "$500",
            status = "Pending"
        ),
        CustomizationRequestItem(
            id = "r2",
            clientName = "Jane Smith",
            description = "Abstract artwork for office",
            budget = "$1000",
            status = "In Progress"
        ),
        CustomizationRequestItem(
            id = "r3",
            clientName = "Mike Johnson",
            description = "Sculpture commission",
            budget = "$2000",
            status = "Completed"
        )
    )
}

data class CustomizationRequestItem(
    val id: String,
    val clientName: String,
    val description: String,
    val budget: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanDashboardScreen(
    viewModel: AuthViewModel,
    context: Context? = null,
    onLogout: () -> Unit,
    onAddArtworkClick: () -> Unit = {}
) {
    val artisanViewModel: ArtisanViewModel = viewModel()
    val artworks = artisanViewModel.artworks.value
    val isLoading = artisanViewModel.isLoading.value
    val requests = getDummyRequests()

    // Refresh artworks when screen is displayed (e.g., when returning from AddArtworkScreen)
    LaunchedEffect(Unit) {
        artisanViewModel.refreshArtworks()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Artisan Dashboard",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        if (context != null) {
                            viewModel.logout(context)
                        }
                        onLogout()
                    }) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Your Artworks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    ShimmerArtworkList(count = 4)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Padding for top section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Your Artworks Section Title
                item {
                    Text(
                        text = "Your Artworks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Empty state or artwork items
                if (artworks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "No artworks yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Text(
                                text = "Click 'Add Artwork' to showcase your creations",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    // Artwork Grid Items
                    items(artworks.chunked(2)) { rowArtworks ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowArtworks.forEach { artwork ->
                                ArtworkCard(
                                    artwork = artwork,
                                    onClick = { /* Handle artwork click */ },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(200.dp)
                                )
                            }
                            // Add spacer if odd number of items
                            if (rowArtworks.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Incoming Requests Section Title
                item {
                    Text(
                        text = "Incoming Requests",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Request Cards
                if (requests.isEmpty()) {
                    item {
                        Text(
                            text = "No requests yet",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                        )
                    }
                } else {
                    items(requests) { request ->
                        RequestCard(
                            request = request,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Add Artwork Button
                item {
                    Button(
                        onClick = onAddArtworkClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "+ Add Artwork",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: CustomizationRequestItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.clientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = request.status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (request.status) {
                        "Pending" -> MaterialTheme.colorScheme.error
                        "In Progress" -> MaterialTheme.colorScheme.tertiary
                        "Completed" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Text(
                text = request.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget: ${request.budget}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* Handle accept */ },
                        modifier = Modifier.height(32.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Accept",
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { /* Handle decline */ },
                        modifier = Modifier.height(32.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Decline",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}









