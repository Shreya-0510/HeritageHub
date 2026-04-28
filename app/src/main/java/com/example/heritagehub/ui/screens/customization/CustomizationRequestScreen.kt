@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.screens.customization

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heritagehub.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationRequestScreen(
    artistName: String,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val description = remember { mutableStateOf("") }
    val budget = remember { mutableStateOf("") }
    val deadline = remember { mutableStateOf("") }
    val isSubmitting = remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Request Customization") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Customize with $artistName", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "The artist will review your request and get back to you.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item {
                OutlinedTextField(
                    value = description.value,
                    onValueChange = { description.value = it },
                    label = { Text("Customization Details") },
                    placeholder = { Text("Describe what you want...") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = budget.value,
                    onValueChange = { budget.value = it },
                    label = { Text("Budget") },
                    placeholder = { Text("e.g. $100 - $200") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = deadline.value,
                    onValueChange = { deadline.value = it },
                    label = { Text("Deadline") },
                    placeholder = { Text("e.g. 2 weeks") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        if (description.value.isNotBlank() && budget.value.isNotBlank() && deadline.value.isNotBlank()) {
                            isSubmitting.value = true
                            scope.launch {
                                try {
                                    val user = auth.currentUser
                                    if (user != null) {
                                        // RELIABLE NAME RESOLUTION
                                        val userDoc = firestore.collection("users").document(user.uid).get().await()
                                        val firestoreName = userDoc.getString("username")
                                        val viewModelName = authViewModel.userName.value
                                        val emailName = user.email?.substringBefore("@")
                                        
                                        val resolvedName = when {
                                            !firestoreName.isNullOrBlank() -> firestoreName
                                            !viewModelName.isNullOrBlank() -> viewModelName
                                            !emailName.isNullOrBlank() -> emailName
                                            else -> "User"
                                        }

                                        val artistId = resolveArtistId(firestore, artistName)
                                        
                                        val request = mapOf(
                                            "artistId" to artistId,
                                            "artistName" to artistName,
                                            "userId" to user.uid,
                                            "userName" to resolvedName,
                                            "description" to description.value,
                                            "budget" to budget.value,
                                            "deadline" to deadline.value,
                                            "status" to "pending",
                                            "createdAt" to System.currentTimeMillis()
                                        )

                                        firestore.collection("customization_requests").add(request).await()
                                        snackbarHostState.showSnackbar("Request sent to $artistName!")
                                        onBack()
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                } finally {
                                    isSubmitting.value = false
                                }
                            }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Please fill in all fields") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isSubmitting.value,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting.value) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    else Text("Send Request", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private suspend fun resolveArtistId(firestore: FirebaseFirestore, artistName: String): String {
    return firestore.collection("users")
        .whereEqualTo("username", artistName)
        .limit(1)
        .get()
        .await()
        .documents
        .firstOrNull()?.id ?: ""
}
