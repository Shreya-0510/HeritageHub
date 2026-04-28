package com.example.heritagehub.ui.screens.artisan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.heritagehub.util.capitalizeWords
import com.example.heritagehub.viewmodel.ArtisanViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProfileScreen(
    viewModel: ArtisanViewModel,
    onBack: () -> Unit
) {
    val artisan = viewModel.currentArtisan.value
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshArtisanProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Profile" else "Your Profile") },
                navigationIcon = {
                    IconButton(onClick = { if (isEditing) isEditing = false else onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEditing && artisan != null) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isEditing) {
                ProfileEditForm(
                    viewModel = viewModel,
                    onSaveComplete = { isEditing = false }
                )
            } else {
                ProfileDisplay(artisan = artisan)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileDisplay(artisan: com.example.heritagehub.model.Artisan?) {
    if (artisan == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Profile Pic
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (artisan.profilePicUrl.isNotEmpty()) {
                AsyncImage(
                    model = artisan.profilePicUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person, 
                    contentDescription = null, 
                    modifier = Modifier.size(80.dp), 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Name
        Text(
            text = artisan.artistName.capitalizeWords(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Location
        if (artisan.location.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    Icons.Default.LocationOn, 
                    contentDescription = null, 
                    modifier = Modifier.size(18.dp), 
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = artisan.location, 
                    style = MaterialTheme.typography.bodyLarge, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // About
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "About You", style = MaterialTheme.typography.titleLarge)
            Text(
                text = artisan.description.ifBlank { "No description added yet." },
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Skills
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Skills", style = MaterialTheme.typography.titleLarge)
            if (artisan.skills.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    artisan.skills.forEach { skill ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = skill,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No skills added yet.", 
                    style = MaterialTheme.typography.bodyLarge, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileEditForm(
    viewModel: ArtisanViewModel,
    onSaveComplete: () -> Unit
) {
    val artisan = viewModel.currentArtisan.value
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isLoading = viewModel.isLoading.value

    var profilePicUri by remember { 
        mutableStateOf<Uri?>(artisan?.profilePicUrl?.takeIf { it.isNotEmpty() }?.let { Uri.parse(it) }) 
    }
    var description by remember { mutableStateOf(artisan?.description ?: "") }
    var skillsText by remember { mutableStateOf(artisan?.skills?.joinToString(", ") ?: "") }
    var location by remember { mutableStateOf(artisan?.location ?: "") }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) profilePicUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Picture Section
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { photoLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (profilePicUri != null && profilePicUri.toString().isNotEmpty()) {
                AsyncImage(
                    model = profilePicUri,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text("Tap to change photo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("About You") },
            placeholder = { Text("Write a brief bio about yourself...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        OutlinedTextField(
            value = skillsText,
            onValueChange = { skillsText = it },
            label = { Text("Skills") },
            placeholder = { Text("e.g. Pottery, Weaving (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            placeholder = { Text("City, State/Region") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                scope.launch {
                    val skills = skillsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    viewModel.updateProfile(
                        context = context,
                        profilePicUri = profilePicUri,
                        description = description,
                        skills = skills,
                        location = location
                    ).onSuccess {
                        onSaveComplete()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Save Profile")
            }
        }
    }
}
