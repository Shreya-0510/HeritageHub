@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.screens.artisan

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import coil.compose.AsyncImage
import com.example.heritagehub.model.Artisan
import com.example.heritagehub.model.Artwork
import com.example.heritagehub.ui.components.ArtworkCard
import com.example.heritagehub.util.capitalizeWords
import com.example.heritagehub.viewmodel.ArtisanDirectoryViewModel
import com.example.heritagehub.viewmodel.HomeViewModel
import java.util.Locale

// Data class for story representation
data class ArtistStory(
    val id: String,
    val title: String,
    val videoUrl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanProfileScreen(
    artistName: String,
    onBack: () -> Unit,
    onNavigateToCustomization: (String) -> Unit = {}
) {
    val directoryViewModel: ArtisanDirectoryViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    var selectedVideoUrl by remember { mutableStateOf<String?>(null) }

    // Refresh data on entry
    LaunchedEffect(Unit) {
        directoryViewModel.refresh()
        homeViewModel.refreshArtworks()
    }

    // Find the artisan from the directory list
    val artisan = directoryViewModel.visibleArtisans.value.find { it.artistName == artistName }
    // Get artworks for this artist
    val artistArtworks = homeViewModel.artworks.value.filter { it.artistName == artistName }

    // Collect all videos from artworks to show as stories
    val realStories = artistArtworks.flatMap { artwork ->
        artwork.allVideoUrls.map { videoUrl ->
            ArtistStory(id = "${artwork.id}_${videoUrl.hashCode()}", title = artwork.title, videoUrl = videoUrl)
        }
    }

    // Fallback stories if none found
    val fallbackStories = listOf(
        ArtistStory("f1", "Crafting Journey"),
        ArtistStory("f2", "Studio Insights"),
        ArtistStory("f3", "Technique Showcase")
    )

    val displayStories = if (realStories.isNotEmpty()) realStories else fallbackStories

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Artist Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section (Profile Pic, Name, Bio)
            item {
                ProfileHeader(artisan = artisan, artistName = artistName)
            }

            // Artworks Section
            item {
                val displayArtworks = if (artistArtworks.isNotEmpty()) artistArtworks else getDummyArtworks(artistName)
                ArtworkWallSection(artworks = displayArtworks)
            }

            // Stories Section (Videos)
            item {
                StoriesSection(
                    stories = displayStories,
                    onStoryClick = { url -> selectedVideoUrl = url }
                )
            }

            // CTA Section
            item {
                Button(
                    onClick = { onNavigateToCustomization(artistName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Request Customization", fontWeight = FontWeight.Bold)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Fullscreen Video Player for Stories
        selectedVideoUrl?.let { url ->
            FullscreenVideoDialog(
                videoUrl = url,
                onDismiss = { selectedVideoUrl = null }
            )
        }
    }
}

@Composable
private fun ProfileHeader(artisan: Artisan?, artistName: String) {
    val rawName = artisan?.artistName ?: artistName
    val nameToDisplay = rawName.capitalizeWords()
    
    val bioToDisplay = if (artisan != null && artisan.description.isNotBlank()) {
        artisan.description
    } else {
        "A dedicated heritage artisan specialized in traditional crafts. Experience the rich culture through my unique hand-crafted creations."
    }
    val locationToDisplay = if (artisan != null && artisan.location.isNotBlank()) {
        artisan.location
    } else {
        "Heritage Artisan"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profile Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val profilePicUrl = artisan?.profilePicUrl
            if (!profilePicUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profilePicUrl,
                    contentDescription = nameToDisplay,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            text = nameToDisplay,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = locationToDisplay,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = bioToDisplay,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun ArtworkWallSection(artworks: List<Artwork>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Artworks",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        artworks.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { artwork ->
                    ArtworkCard(
                        artwork = artwork,
                        modifier = Modifier.weight(1f),
                        onClick = { /* Detail navigation if needed */ }
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StoriesSection(
    stories: List<ArtistStory>,
    onStoryClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Stories",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            items(stories) { story ->
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .clickable { if (story.videoUrl.isNotEmpty()) onStoryClick(story.videoUrl) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = story.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
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
                playbackError = error.message ?: "Unsupported video format on this device"
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
    // Robust resolution: If it's a local file, let ExoPlayer detect the MIME type from the file content
    return if (uri.scheme == "file" || videoUrl.startsWith("/")) {
        MediaItem.fromUri(uri)
    } else {
        // For network streams, try to provide a hint
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

private fun getDummyArtworks(artistName: String): List<Artwork> {
    return listOf(
        Artwork("d1", "Traditional Pottery", artistName, ""),
        Artwork("d2", "Handwoven Silk", artistName, ""),
        Artwork("d3", "Stone Carving", artistName, ""),
        Artwork("d4", "Wood Sculpture", artistName, "")
    )
}
