@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.screens.artisan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heritagehub.model.Artwork
import com.example.heritagehub.ui.components.ArtworkCard

// Dummy data - returns all artworks for an artist
fun getArtworksForArtist(artistName: String): List<Artwork> {
    val allArtworks = listOf(
        Artwork("1", "Sunset Over Mountains", "Alice Chen", "https://example.com/sunset.jpg"),
        Artwork("2", "Urban Dreams", "Marcus Johnson", "https://example.com/urban.jpg"),
        Artwork("3", "Nature's Whisper", "Elena Rodriguez", "https://example.com/nature.jpg"),
        Artwork("4", "Abstract Harmony", "James Wilson", "https://example.com/abstract.jpg"),
        Artwork("5", "Coastal Beauty", "Sofia Garcia", "https://example.com/coastal.jpg"),
        Artwork("6", "Starry Night", "David Kim", "https://example.com/stars.jpg"),
        Artwork("7", "Forest Trail", "Maya Patel", "https://example.com/forest.jpg"),
        Artwork("8", "City Lights", "Lucas Brown", "https://example.com/city.jpg"),
        Artwork("9", "Desert Rose", "Isabella Santos", "https://example.com/desert.jpg"),
        Artwork("10", "Ocean Waves", "Kai Tanaka", "https://example.com/ocean.jpg"),
    )
    return allArtworks.filter { it.artistName == artistName }
}

// Story data class for placeholder
data class ArtistStory(
    val id: String,
    val title: String,
    val thumbnailUrl: String
)

// Get dummy stories for an artist
@Suppress("UNUSED_PARAMETER")
fun getStoriesForArtist(artistName: String): List<ArtistStory> {
    return listOf(
        ArtistStory("1", "My Craft Journey", ""),
        ArtistStory("2", "Behind the Scenes", ""),
        ArtistStory("3", "Creative Process", ""),
        ArtistStory("4", "Studio Tour", "")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanProfileScreen(
    artistName: String,
    onBack: () -> Unit,
    onNavigateToCustomization: (String) -> Unit = {}
) {
    val artworks = remember { getArtworksForArtist(artistName) }
    val stories = remember { getStoriesForArtist(artistName) }

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
            // Header Section
            item {
                ProfileHeader(artistName = artistName)
            }

            // Artworks Section
            if (artworks.isNotEmpty()) {
                item {
                    ArtworkWallSection(artworks = artworks)
                }
            }

            // Stories Section
            item {
                StoriesSection(stories = stories)
            }

            // CTA Section
            item {
                CustomizationButton(artistName = artistName, onNavigateToCustomization = onNavigateToCustomization)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeader(artistName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profile Image (Circle Placeholder)
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = artistName.first().toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Artist Name
        Text(
            text = artistName,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Location
        Text(
            text = "Heritage Artisan",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Bio
        Text(
            text = "A passionate artist dedicated to preserving cultural heritage through contemporary art. Creating meaningful pieces that blend tradition with innovation.",
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

        // 2-column grid layout
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            artworks.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(2) { index ->
                        if (index < row.size) {
                            ArtworkCard(
                                artwork = row[index],
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                onClick = { /* Handle artwork click */ }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoriesSection(stories: List<ArtistStory>) {
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
                StoryCard(story = story)
            }
        }
    }
}

@Composable
private fun StoryCard(story: ArtistStory) {
    Card(
        modifier = Modifier
            .width(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Video Thumbnail Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶",
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Story Title
            Text(
                text = story.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun CustomizationButton(
    artistName: String,
    onNavigateToCustomization: (String) -> Unit = {}
) {
    Button(
        onClick = { onNavigateToCustomization(artistName) },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            "Request Customization",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}





