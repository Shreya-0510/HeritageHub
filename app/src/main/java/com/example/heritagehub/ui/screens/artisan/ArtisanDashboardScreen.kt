@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.screens.artisan

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
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
import com.example.heritagehub.util.capitalizeWords
import com.example.heritagehub.viewmodel.AuthViewModel
import com.example.heritagehub.viewmodel.ArtisanViewModel

data class CustomizationRequestItem(
    val id: String,
    val userName: String,
    val description: String,
    val budget: String,
    val deadline: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanDashboardScreen(
    viewModel: AuthViewModel,
    context: Context? = null,
    onLogout: () -> Unit,
    onAddArtworkClick: () -> Unit = {},
    onArtworkClick: (Artwork) -> Unit,
    onManageProfileClick: () -> Unit = {}
) {
    val artisanViewModel: ArtisanViewModel = viewModel()
    val artworks = artisanViewModel.artworks.value
    val incomingRequests = artisanViewModel.requests.value
    val isLoading = artisanViewModel.isLoading.value

    LaunchedEffect(Unit) {
        artisanViewModel.refreshArtworks()
        artisanViewModel.refreshCustomizationRequests()
        artisanViewModel.refreshArtisanProfile()
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
                    IconButton(onClick = onManageProfileClick) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Manage Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Artworks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (artworks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No artworks yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(artworks.chunked(2)) { rowArtworks ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowArtworks.forEach { artwork ->
                                ArtworkCard(
                                    artwork = artwork,
                                    onClick = { onArtworkClick(artwork) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowArtworks.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                item {
                    Text(
                        text = "Incoming Requests",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    )
                }

                if (incomingRequests.isEmpty()) {
                    item {
                        Text(
                            text = "No requests yet",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                        )
                    }
                } else {
                    items(incomingRequests) { request ->
                        RequestCard(
                            request = CustomizationRequestItem(
                                id = request.id,
                                userName = request.userName,
                                description = request.description,
                                budget = request.budget,
                                deadline = request.deadline,
                                status = request.status
                            ),
                            onStatusUpdate = { newStatus ->
                                artisanViewModel.updateRequestStatus(request.id, newStatus)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                item {
                    Button(
                        onClick = onAddArtworkClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("+ Add Artwork", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: CustomizationRequestItem,
    onStatusUpdate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                // Capitalize the username here
                val displayName = request.userName.ifBlank { "Unknown User" }.capitalizeWords()
                Text(
                    text = "Request from $displayName",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = request.status.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (request.status.lowercase()) {
                        "pending" -> MaterialTheme.colorScheme.error
                        "accepted" -> MaterialTheme.colorScheme.primary
                        "declined" -> MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Text(text = request.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Budget: ${request.budget}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Deadline: ${request.deadline}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (request.status.lowercase() == "pending") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onStatusUpdate("accepted") },
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Accept", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onStatusUpdate("declined") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Decline", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }
    }
}
