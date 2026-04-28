package com.example.heritagehub.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.heritagehub.data.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val isLoading = mutableStateOf(false)
    val isAuthChecking = mutableStateOf(true)
    val error = mutableStateOf<String?>(null)
    val isAuthenticated = mutableStateOf(false)
    val userRole = mutableStateOf<String?>(null)
    val userName = mutableStateOf<String?>(null)
    val userEmail = mutableStateOf<String?>(null)
    val userId = mutableStateOf<String?>(null)

    // Saved credentials for "Remember Me" pre-filling
    val savedIdentifier = mutableStateOf("")
    val isRememberMeChecked = mutableStateOf(false)

    init {
        checkAuthStatus()
    }

    fun loadSavedCredentials(context: Context) {
        isRememberMeChecked.value = PreferencesManager.isRememberMeEnabled(context)
        if (isRememberMeChecked.value) {
            savedIdentifier.value = PreferencesManager.getSavedIdentifier(context) ?: ""
        }
    }

    private fun fetchUserRole(uid: String) {
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userRole.value = document.getString("role") ?: "user"
                    userName.value = document.getString("username") ?: ""
                    userEmail.value = document.getString("email") ?: ""
                } else {
                    userRole.value = "user"
                }
                isAuthChecking.value = false
            }
            .addOnFailureListener {
                userRole.value = "user"
                isAuthChecking.value = false
            }
    }

    fun checkAuthStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            isAuthenticated.value = true
            userId.value = currentUser.uid
            fetchUserRole(currentUser.uid)
        } else {
            isAuthenticated.value = false
            userRole.value = null
            isAuthChecking.value = false
        }
    }

    fun checkUsernameAvailability(
        username: String,
        callback: (isAvailable: Boolean, message: String) -> Unit
    ) {
        if (username.length < 3) {
            callback(false, "Username must be at least 3 characters")
            return
        }

        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    callback(true, "Username available")
                } else {
                    callback(false, "Username already taken")
                }
            }
            .addOnFailureListener {
                callback(false, "Error checking username")
            }
    }

    fun signupWithEmail(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String = "user",
        onSuccess: () -> Unit
    ) {
        if (password != confirmPassword) {
            error.value = "Passwords do not match"
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
                val userIdVal = result.user?.uid ?: return@addOnSuccessListener
                userId.value = userIdVal
                userEmail.value = email
                userName.value = username

                val userData = mapOf(
                    "username" to username,
                    "email" to email,
                    "role" to role,
                    "authType" to "email",
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("users").document(userIdVal)
                    .set(userData)
                    .addOnSuccessListener {
                        userRole.value = role
                        isLoading.value = false
                        isAuthenticated.value = true
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        isLoading.value = false
                        error.value = exception.message ?: "Failed to create account"
                    }
            }
            .addOnFailureListener { exception ->
                isLoading.value = false
                error.value = exception.message ?: "Failed to create account"
            }
    }

    fun loginWithEmail(
        emailOrUsername: String,
        password: String,
        rememberMeEnabled: Boolean,
        context: Context,
        onSuccess: () -> Unit
    ) {
        val identifier = emailOrUsername.trim()

        if (identifier.isEmpty() || password.isEmpty()) {
            error.value = "Please enter both credentials"
            return
        }

        isLoading.value = true
        error.value = null

        if (identifier.contains("@")) {
            performEmailLogin(identifier, password, identifier, rememberMeEnabled, context, onSuccess)
        } else {
            firestore.collection("users")
                .whereEqualTo("username", identifier)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        isLoading.value = false
                        error.value = "No account found for that username"
                    } else {
                        val email = snapshot.documents[0].getString("email")
                        if (email != null) {
                            performEmailLogin(email, password, identifier, rememberMeEnabled, context, onSuccess)
                        } else {
                            isLoading.value = false
                            error.value = "Email not found for this user"
                        }
                    }
                }
                .addOnFailureListener {
                    isLoading.value = false
                    error.value = "Unable to validate username"
                }
        }
    }

    private fun performEmailLogin(
        email: String,
        password: String,
        typedIdentifier: String,
        rememberMeEnabled: Boolean,
        context: Context,
        onSuccess: () -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userIdVal = result.user?.uid ?: return@addOnSuccessListener
                userId.value = userIdVal
                userEmail.value = email

                firestore.collection("users").document(userIdVal)
                    .get()
                    .addOnSuccessListener { document ->
                        userRole.value = document.getString("role") ?: "user"
                        userName.value = document.getString("username") ?: ""
                        
                        if (rememberMeEnabled) {
                            PreferencesManager.setRememberMe(context, true)
                            PreferencesManager.setSavedIdentifier(context, typedIdentifier)
                        } else {
                            PreferencesManager.clearSavedCredentials(context)
                        }

                        isLoading.value = false
                        isAuthenticated.value = true
                        onSuccess()
                    }
                    .addOnFailureListener {
                        isLoading.value = false
                        error.value = "Failed to fetch user data"
                    }
            }
            .addOnFailureListener { exception ->
                isLoading.value = false
                error.value = exception.message ?: "Login failed"
            }
    }

    fun logout(context: Context) {
        PreferencesManager.clearSavedCredentials(context)
        savedIdentifier.value = ""
        isRememberMeChecked.value = false
        
        auth.signOut()
        isAuthenticated.value = false
        userRole.value = null
        userName.value = null
        userEmail.value = null
        userId.value = null
        error.value = null
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        if (email.isBlank()) {
            error.value = "Please enter your email"
            return
        }
        isLoading.value = true
        error.value = null
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener {
                isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { exception ->
                isLoading.value = false
                error.value = exception.message ?: "Failed to send reset email"
            }
    }
}
