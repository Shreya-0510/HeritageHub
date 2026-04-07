package com.example.heritagehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val isAuthenticated = mutableStateOf(false)
    val userRole = mutableStateOf<String?>(null)

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            error.value = "Please fill in all fields"
            return
        }

        isLoading.value = true
        error.value = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                // Fetch user role from Firestore
                firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        val role = document.getString("role") ?: "user"
                        userRole.value = role
                        isLoading.value = false
                        isAuthenticated.value = true
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        isLoading.value = false
                        error.value = exception.message ?: "Failed to fetch user data"
                    }
            }
            .addOnFailureListener { exception ->
                isLoading.value = false
                error.value = exception.message ?: "Login failed"
            }
    }

    fun signup(email: String, password: String, role: String, onSuccess: () -> Unit) {
        if (email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            error.value = "Please fill in all fields"
            return
        }

        if (password.length < 6) {
            error.value = "Password must be at least 6 characters"
            return
        }

        isLoading.value = true
        error.value = null

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                val userData = mapOf(
                    "email" to email,
                    "role" to role,
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("users").document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        userRole.value = role
                        isLoading.value = false
                        isAuthenticated.value = true
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        isLoading.value = false
                        error.value = exception.message ?: "Signup failed"
                    }
            }
            .addOnFailureListener { exception ->
                isLoading.value = false
                error.value = exception.message ?: "Signup failed"
            }
    }

    fun logout() {
        auth.signOut()
        isAuthenticated.value = false
        userRole.value = null
        error.value = null
    }

    fun checkAuthStatus() {
        isAuthenticated.value = auth.currentUser != null
        if (isAuthenticated.value) {
            // Fetch user role on app startup
            val userId = auth.currentUser?.uid ?: return
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    userRole.value = document.getString("role") ?: "user"
                }
                .addOnFailureListener {
                    userRole.value = "user" // Default to user role if fetch fails
                }
        }
    }
}

