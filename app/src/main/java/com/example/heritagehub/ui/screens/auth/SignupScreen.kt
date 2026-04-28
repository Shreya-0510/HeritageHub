@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heritagehub.viewmodel.AuthViewModel

@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onProceed: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val username = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val selectedRole = remember { mutableStateOf("user") }
    val usernameAvailable = remember { mutableStateOf<Boolean?>(null) }
    val usernameCheckMessage = remember { mutableStateOf("") }
    val showPassword = remember { mutableStateOf(false) }
    val showConfirmPassword = remember { mutableStateOf(false) }
    val isLoading = viewModel.isLoading.value
    val errorMessage = viewModel.error.value

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 4.dp)
            )

            Text(
                text = "Join Heritage Hub",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 24.dp)
            )

            // ==================== USERNAME FIELD ====================
            OutlinedTextField(
                value = username.value,
                onValueChange = { newUsername ->
                    username.value = newUsername
                    if (newUsername.length >= 3) {
                        viewModel.checkUsernameAvailability(newUsername) { isAvailable, message ->
                            usernameAvailable.value = isAvailable
                            usernameCheckMessage.value = message
                        }
                    } else {
                        usernameAvailable.value = null
                        usernameCheckMessage.value = ""
                    }
                },
                label = { Text("Username") },
                placeholder = { Text("Choose a unique username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                enabled = !isLoading,
                singleLine = true,
                isError = usernameAvailable.value == false
            )

            if (usernameCheckMessage.value.isNotEmpty()) {
                Text(
                    text = usernameCheckMessage.value,
                    fontSize = 11.sp,
                    color = if (usernameAvailable.value == true)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 12.dp)
                )
            }

            // ==================== EMAIL FIELD ====================
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                placeholder = { Text("your@email.com") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                enabled = !isLoading,
                singleLine = true
            )

            // ==================== PASSWORD FIELD ====================
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                placeholder = { Text("Minimum 6 characters") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                enabled = !isLoading,
                singleLine = true,
                visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = { showPassword.value = !showPassword.value },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = if (showPassword.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showPassword.value) "Hide password" else "Show password"
                        )
                    }
                }
            )

            // ==================== CONFIRM PASSWORD FIELD ====================
            OutlinedTextField(
                value = confirmPassword.value,
                onValueChange = { confirmPassword.value = it },
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !isLoading,
                singleLine = true,
                visualTransformation = if (showConfirmPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = { showConfirmPassword.value = !showConfirmPassword.value },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = if (showConfirmPassword.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showConfirmPassword.value) "Hide password" else "Show password"
                        )
                    }
                }
            )

            // ==================== ROLE SELECTION ====================
            Text(
                text = "Account Type",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Role
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = selectedRole.value == "user",
                            onClick = { selectedRole.value = "user" },
                            role = Role.RadioButton
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedRole.value == "user",
                        onClick = { selectedRole.value = "user" },
                        enabled = !isLoading
                    )
                    Text(
                        text = "User",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Artisan Role
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = selectedRole.value == "artisan",
                            onClick = { selectedRole.value = "artisan" },
                            role = Role.RadioButton
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedRole.value == "artisan",
                        onClick = { selectedRole.value = "artisan" },
                        enabled = !isLoading
                    )
                    Text(
                        text = "Artisan",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // ==================== ERROR MESSAGE ====================
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // ==================== CREATE ACCOUNT BUTTON (Default Email Signup) ====================
            Button(
                onClick = {
                    if (usernameAvailable.value == true && email.value.isNotEmpty() &&
                        username.value.isNotEmpty() && password.value.isNotEmpty() && confirmPassword.value.isNotEmpty()) {
                        if (password.value != confirmPassword.value) {
                            viewModel.error.value = "Passwords do not match"
                        } else if (password.value.length < 6) {
                            viewModel.error.value = "Password must be at least 6 characters"
                        } else {
                            viewModel.signupWithEmail(
                                username.value,
                                email.value,
                                password.value,
                                confirmPassword.value,
                                selectedRole.value
                            ) {
                                onProceed()
                            }
                        }
                    } else if (usernameAvailable.value != true) {
                        viewModel.error.value = "Please choose an available username"
                    } else {
                        viewModel.error.value = "Please fill in all fields"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                enabled = !isLoading && usernameAvailable.value == true && email.value.isNotEmpty() &&
                        password.value.isNotEmpty() && confirmPassword.value.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(4.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create Account")
                }
            }

            TextButton(onClick = onNavigateToLogin, enabled = !isLoading, modifier = Modifier.padding(top = 12.dp)) {
                Text("Already have an account? Sign in")
            }
        }
    }
}
