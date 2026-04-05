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

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            error.value = "Please fill in all fields"
            return
        }

        isLoading.value = true
        error.value = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                isLoading.value = false
                isAuthenticated.value = true
                onSuccess()
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
        error.value = null
    }

    fun checkAuthStatus() {
        isAuthenticated.value = auth.currentUser != null
    }
}

