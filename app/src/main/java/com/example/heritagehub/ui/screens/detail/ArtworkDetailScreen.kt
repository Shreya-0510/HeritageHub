@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heritagehub.viewmodel.CartViewModel
import com.example.heritagehub.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkDetailScreen(
    artworkId: String,
    onBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToCustomization: (String) -> Unit = {},
    onNavigateToCart: () -> Unit = {}
) {
    val homeViewModel: HomeViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val artworks = homeViewModel.artworks.value
    val snackbarHostState = remember { SnackbarHostState() }
    val cartMessage = cartViewModel.message.value

    LaunchedEffect(cartMessage) {
        if (!cartMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(cartMessage)
            cartViewModel.consumeMessage()
        }
    }

    val artwork = artworks.find { it.id == artworkId }

    if (artwork == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Artwork not found")
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item { ImageCarousel() }

            item { PageIndicator(currentPage = 0, totalPages = 3) }

            item {
                Text(
                    text = artwork.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Text(
                    text = "by ${artwork.artistName}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        onNavigateToProfile(artwork.artistName)
                    }
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Text(artwork.category)
                    }

                    if (artwork.customizationAvailable) {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Text("✓ Customizable")
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Price")
                    Text(
                        artwork.price,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                Column {
                    Text("Description", fontWeight = FontWeight.SemiBold)
                    Text(artwork.description)
                }
            }

            item {
                Column {
                    Text("About the Artist", fontWeight = FontWeight.SemiBold)
                    Text("${artwork.artistName} is a talented artisan.")
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            // Keep the original Buy Now intent: add item then jump to cart.
                            cartViewModel.addToCart(artwork) { success ->
                                if (success) {
                                    onNavigateToCart()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Buy Now")
                    }

                    if (artwork.customizationAvailable) {
                        Button(
                            onClick = {
                                onNavigateToCustomization(artwork.artistName)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Request Customization")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageCarousel() {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(3) {
            Box(
                modifier = Modifier
                    .height(300.dp)
                    .width(250.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("Image")
            }
        }
    }
}

@Composable
private fun PageIndicator(currentPage: Int, totalPages: Int) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        repeat(totalPages) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .height(8.dp)
                    .width(if (it == currentPage) 24.dp else 8.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}