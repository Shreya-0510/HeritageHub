@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.screens.detail

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.heritagehub.viewmodel.CartViewModel
import com.example.heritagehub.viewmodel.HomeViewModel
import coil.compose.AsyncImage
import com.example.heritagehub.util.capitalizeWords
import java.util.Locale

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
    val imageUrls = artwork?.allImageUrls.orEmpty()
    val videoUrls = artwork?.allVideoUrls.orEmpty()

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

            item { ImageCarousel(imageUrls = imageUrls, title = artwork.title) }

            if (imageUrls.size > 1) {
                item { PageIndicator(currentPage = 0, totalPages = imageUrls.size) }
            }

            item {
                Text(
                    text = artwork.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                // Capitalized Artist Name
                Text(
                    text = "by ${artwork.artistName.capitalizeWords()}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
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

            if (videoUrls.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Videos", fontWeight = FontWeight.SemiBold)
                        VideoList(videoUrls = videoUrls)
                    }
                }
            }



            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
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
private fun ImageCarousel(imageUrls: List<String>, title: String) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (imageUrls.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .width(250.dp)
                        .clip(RoundedCornerShape(16.dp))
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
                        .height(300.dp)
                        .width(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }
    }
}

@Composable
private fun VideoList(videoUrls: List<String>) {
    var selectedVideoUrl by remember { mutableStateOf<String?>(null) }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(videoUrls) { url ->
            VideoPreviewCard(
                label = "Video",
                onClick = { selectedVideoUrl = url }
            )
        }
    }

    selectedVideoUrl?.let { url ->
        FullscreenVideoDialog(
            videoUrl = url,
            onDismiss = { selectedVideoUrl = null }
        )
    }
}

@Composable
private fun VideoPreviewCard(
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Text("$label - Tap to play", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FullscreenVideoDialog(
    videoUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var playbackError by remember(videoUrl) { mutableStateOf<String?>(null) }

    val player = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(buildMediaItem(videoUrl))
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
            prepare()
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                playbackError = error.localizedMessage ?: "Unsupported video format"
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { viewContext ->
                        PlayerView(viewContext).apply {
                            this.player = player
                            useController = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                if (playbackError != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Playback Error: $playbackError",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

private fun buildMediaItem(videoUrl: String): MediaItem {
    val uri = Uri.parse(videoUrl)
    // For local files, Media3 works best when you let it detect the container automatically.
    // Explicitly setting MIME type often breaks things if the file extension doesn't match perfectly.
    return if (uri.scheme == "file" || videoUrl.startsWith("/")) {
        MediaItem.fromUri(uri)
    } else {
        val ext = MimeTypeMap.getFileExtensionFromUrl(videoUrl).lowercase()
        val mimeType = when (ext) {
            "mp4", "m4v" -> "video/mp4"
            "webm" -> "video/webm"
            "mkv" -> "video/x-matroska"
            else -> null
        }
        val builder = MediaItem.Builder().setUri(uri)
        if (mimeType != null) builder.setMimeType(mimeType)
        builder.build()
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
