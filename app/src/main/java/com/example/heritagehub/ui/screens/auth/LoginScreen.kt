@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heritagehub.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val isLoading = viewModel.isLoading.value
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadSavedCredentials(context)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "Sign in to your account",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            EmailPasswordLoginTab(
                viewModel = viewModel,
                context = context,
                onLoginSuccess = onLoginSuccess,
                onNavigateToForgotPassword = onNavigateToForgotPassword
            )

            if (viewModel.error.value != null) {
                Text(
                    text = viewModel.error.value ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = onNavigateToSignup,
                enabled = !isLoading,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text("Don't have an account? Sign up")
            }
        }
    }
}

@Composable
private fun EmailPasswordLoginTab(
    viewModel: AuthViewModel,
    context: android.content.Context,
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val emailOrUsername = remember(viewModel.savedIdentifier.value) { 
        mutableStateOf(viewModel.savedIdentifier.value) 
    }
    val password = remember { mutableStateOf("") }
    val showPassword = remember { mutableStateOf(false) }
    val rememberMe = remember(viewModel.isRememberMeChecked.value) { 
        mutableStateOf(viewModel.isRememberMeChecked.value) 
    }
    val isLoading = viewModel.isLoading.value

    OutlinedTextField(
        value = emailOrUsername.value,
        onValueChange = { emailOrUsername.value = it },
        label = { Text("Email or Username") },
        placeholder = { Text("your@email.com or username") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp),
        enabled = !isLoading,
        singleLine = true
    )

    OutlinedTextField(
        value = password.value,
        onValueChange = { password.value = it },
        label = { Text("Password") },
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Checkbox(
                checked = rememberMe.value,
                onCheckedChange = { rememberMe.value = it },
                enabled = !isLoading
            )
            Text(
                text = "Remember me",
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        TextButton(onClick = onNavigateToForgotPassword, enabled = !isLoading) {
            Text("Forgot Password?", fontSize = 12.sp)
        }
    }

    Button(
        onClick = {
            viewModel.loginWithEmail(
                emailOrUsername = emailOrUsername.value,
                password = password.value,
                rememberMeEnabled = rememberMe.value,
                context = context,
                onSuccess = onLoginSuccess
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        enabled = !isLoading && emailOrUsername.value.isNotEmpty() && password.value.isNotEmpty()
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(4.dp), strokeWidth = 2.dp)
        } else {
            Text("Sign In")
        }
    }
}
