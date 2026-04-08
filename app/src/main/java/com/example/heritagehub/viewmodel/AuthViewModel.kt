package com.example.heritagehub.viewmodel

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val isAuthenticated = mutableStateOf(false)
    val userRole = mutableStateOf<String?>(null)
    val userName = mutableStateOf<String?>(null)
    val userEmail = mutableStateOf<String?>(null)
    val userId = mutableStateOf<String?>(null)
    val rememberMe = mutableStateOf(false)
    val verificationId = mutableStateOf<String?>(null)
    val phoneNumber = mutableStateOf<String?>(null)

    fun checkAuthStatus(context: Context? = null) {
        isAuthenticated.value = auth.currentUser != null
        if (isAuthenticated.value) {
            // Fetch user role on app startup
            val userIdVal = auth.currentUser?.uid ?: return
            userId.value = userIdVal
            firestore.collection("users").document(userIdVal)
                .get()
                .addOnSuccessListener { document ->
                    userRole.value = document.getString("role") ?: "user"
                    userName.value = document.getString("username") ?: ""
                    userEmail.value = document.getString("email") ?: ""
                }
                .addOnFailureListener {
                    userRole.value = "user" // Default to user role if fetch fails
                }
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
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading.value = true
        error.value = null

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

    @Suppress("UNUSED_PARAMETER")
    fun loginWithEmail(
        emailOrUsername: String,
        password: String,
        rememberMeEnabled: Boolean,
        context: Context,
        onSuccess: () -> Unit
    ) {
        val identifier = emailOrUsername.trim()
        rememberMe.value = rememberMeEnabled

        if (identifier.isEmpty() || password.isEmpty()) {
            error.value = "Please enter both credentials"
            return
        }

        if (identifier.contains("@")) {
            loginWithEmail(email = identifier, password = password, onSuccess = onSuccess)
            return
        }

        isLoading.value = true
        error.value = null
        firestore.collection("users")
            .whereEqualTo("username", identifier)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val resolvedEmail = snapshot.documents.firstOrNull()?.getString("email")
                if (resolvedEmail.isNullOrBlank()) {
                    isLoading.value = false
                    error.value = "No account found for that username"
                    return@addOnSuccessListener
                }
                loginWithEmail(email = resolvedEmail, password = password, onSuccess = onSuccess)
            }
            .addOnFailureListener {
                isLoading.value = false
                error.value = "Unable to validate username"
            }
    }

    fun requestPhoneOtp(
        phoneNum: String,
        activity: Activity,
        onCodeSent: () -> Unit
    ) {
        isLoading.value = true
        error.value = null
        phoneNumber.value = phoneNum

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                signInWithPhoneCredential(credential) {
                    isLoading.value = false
                    isAuthenticated.value = true
                }
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                isLoading.value = false
                error.value = e.message ?: "Phone verification failed"
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@AuthViewModel.verificationId.value = verificationId
                isLoading.value = false
                onCodeSent()
            }
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNum,
            60,
            TimeUnit.SECONDS,
            activity,
            callbacks
        )
    }

    fun verifyPhoneOtp(
        otp: String,
        username: String,
        email: String,
        role: String = "user",
        onSuccess: () -> Unit
    ) {
        isLoading.value = true
        error.value = null

        val verificationIdVal = verificationId.value ?: run {
            isLoading.value = false
            error.value = "Verification ID not found"
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationIdVal, otp)
        signInWithPhoneCredential(credential) {
            val userIdVal = auth.currentUser?.uid ?: return@signInWithPhoneCredential
            userId.value = userIdVal
            userName.value = username
            userEmail.value = email

            val userData = mapOf(
                "username" to username,
                "email" to email,
                "phoneNumber" to phoneNumber.value,
                "role" to role,
                "authType" to "phone",
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
                .addOnFailureListener {
                    isLoading.value = false
                    error.value = "Failed to create account"
                }
        }
    }

    private fun signInWithPhoneCredential(
        credential: com.google.firebase.auth.PhoneAuthCredential,
        onComplete: () -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                onComplete()
            }
            .addOnFailureListener { exception ->
                isLoading.value = false
                error.value = exception.message ?: "Phone sign-in failed"
            }
    }

    fun resetPassword(
        email: String,
        onSuccess: () -> Unit
    ) {
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

    fun signupWithGoogle(
        idToken: String,
        displayName: String,
        role: String = "user",
        onSuccess: () -> Unit
    ) {
        if (idToken.isBlank()) {
            error.value = "Google sign-in token not available"
            return
        }

        val resolvedEmail = userEmail.value.orEmpty()
        val resolvedName = if (displayName.isBlank()) {
            resolvedEmail.substringBefore("@").ifBlank { "user" }
        } else {
            displayName
        }

        signInWithGoogle(
            idToken = idToken,
            username = resolvedName,
            email = resolvedEmail,
            role = role,
            onSuccess = onSuccess
        )
    }

    fun logout(context: Context) {
        auth.signOut()
        isAuthenticated.value = false
        userRole.value = null
        userName.value = null
        userEmail.value = null
        userId.value = null
        error.value = null
    }

    fun signInWithGoogle(
        idToken: String,
        username: String,
        email: String,
        role: String = "user",
        onSuccess: () -> Unit
    ) {
        isLoading.value = true
        error.value = null

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val userIdVal = result.user?.uid ?: return@addOnSuccessListener
                userId.value = userIdVal
                userEmail.value = email
                userName.value = username

                val userData = mapOf(
                    "username" to username,
                    "email" to email,
                    "role" to role,
                    "authType" to "google",
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
                    .addOnFailureListener {
                        isLoading.value = false
                        error.value = "Failed to create account"
                    }
            }
            .addOnFailureListener { exception ->
                isLoading.value = false
                error.value = exception.message ?: "Google sign-in failed"
            }
    }

    fun loginWithPhoneOtp(
        code: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        val verificationIdVal = verificationId.value
        if (verificationIdVal.isNullOrBlank()) {
            error.value = "Request OTP first"
            return
        }
        if (code.length != 6) {
            error.value = "Please enter a valid 6-digit code"
            return
        }

        isLoading.value = true
        error.value = null

        val credential = PhoneAuthProvider.getCredential(verificationIdVal, code)
        signInWithPhoneCredential(credential) {
            val userIdVal = auth.currentUser?.uid
            if (userIdVal.isNullOrBlank()) {
                isLoading.value = false
                error.value = "Unable to complete sign in"
                return@signInWithPhoneCredential
            }

            userId.value = userIdVal
            firestore.collection("users").document(userIdVal)
                .get()
                .addOnSuccessListener { document ->
                    userRole.value = document.getString("role") ?: "user"
                    userName.value = document.getString("username") ?: ""
                    userEmail.value = document.getString("email") ?: ""
                    isLoading.value = false
                    isAuthenticated.value = true
                    onSuccess()
                }
                .addOnFailureListener {
                    isLoading.value = false
                    error.value = "Failed to fetch user data"
                }
        }
    }
}
