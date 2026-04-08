@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    val selectedTab = remember { mutableStateOf(0) } // 0 = Email/Password, 1 = Mobile OTP
    val isLoading = viewModel.isLoading.value
    val context = LocalContext.current

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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

            // Tab selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabButton(
                    text = "Email / Username",
                    isSelected = selectedTab.value == 0,
                    onClick = { selectedTab.value = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Mobile OTP",
                    isSelected = selectedTab.value == 1,
                    onClick = { selectedTab.value = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedTab.value == 0) {
                EmailPasswordLoginTab(
                    viewModel = viewModel,
                    context = context,
                    onLoginSuccess = onLoginSuccess,
                    onNavigateToForgotPassword = onNavigateToForgotPassword
                )
            } else {
                MobileOtpLoginTab(
                    viewModel = viewModel,
                    context = context,
                    onLoginSuccess = onLoginSuccess
                )
            }

            if (viewModel.error.value != null) {
                Text(
                    text = viewModel.error.value ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            TextButton(onClick = onNavigateToSignup, enabled = !isLoading, modifier = Modifier.padding(top = 16.dp)) {
                Text("Don't have an account? Sign up")
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmailPasswordLoginTab(
    viewModel: AuthViewModel,
    context: android.content.Context,
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val emailOrUsername = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val showPassword = remember { mutableStateOf(false) }
    val rememberMe = remember { mutableStateOf(false) }
    val isLoading = viewModel.isLoading.value

    OutlinedTextField(
        value = emailOrUsername.value,
        onValueChange = { emailOrUsername.value = it },
        label = { Text("Email or Username") },
        placeholder = { Text("your@email.com or username") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
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
            .padding(vertical = 12.dp),
        enabled = !isLoading && emailOrUsername.value.isNotEmpty() && password.value.isNotEmpty()
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(4.dp), strokeWidth = 2.dp)
        } else {
            Text("Sign In")
        }
    }
}

@Composable
fun MobileOtpLoginTab(
    viewModel: AuthViewModel,
    context: android.content.Context,
    onLoginSuccess: () -> Unit
) {
    val phoneNumber = remember { mutableStateOf("") }
    val otp = remember { mutableStateOf("") }
    val showOtpInput = remember { mutableStateOf(false) }
    val isLoading = viewModel.isLoading.value
    val activity = (context as? android.app.Activity)

    if (!showOtpInput.value) {
        OutlinedTextField(
            value = phoneNumber.value,
            onValueChange = { phoneNumber.value = it },
            label = { Text("Mobile Number") },
            placeholder = { Text("+1 (555) 000-0000") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            enabled = !isLoading,
            singleLine = true,
            prefix = { Text("+") }
        )

        Button(
            onClick = {
                if (phoneNumber.value.isNotEmpty() && activity != null) {
                    viewModel.requestPhoneOtp(
                        phoneNum = "+${phoneNumber.value.filter { it.isDigit() }}",
                        activity = activity,
                        onCodeSent = { showOtpInput.value = true }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            enabled = !isLoading && phoneNumber.value.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp), strokeWidth = 2.dp)
            } else {
                Text("Send OTP")
            }
        }
    } else {
        OutlinedTextField(
            value = otp.value,
            onValueChange = { otp.value = it.take(6) },
            label = { Text("Verification Code") },
            placeholder = { Text("000000") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            enabled = !isLoading,
            singleLine = true
        )

        Button(
            onClick = {
                if (otp.value.length == 6) {
                    viewModel.loginWithPhoneOtp(
                        code = otp.value,
                        context = context,
                        onSuccess = onLoginSuccess
                    )
                } else {
                    viewModel.error.value = "Please enter a valid 6-digit code"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            enabled = !isLoading && otp.value.length == 6
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp), strokeWidth = 2.dp)
            } else {
                Text("Verify & Sign In")
            }
        }

        TextButton(
            onClick = {
                showOtpInput.value = false
                phoneNumber.value = ""
                otp.value = ""
            },
            enabled = !isLoading
        ) {
            Text("Use different number", fontSize = 12.sp)
        }
    }
}
















