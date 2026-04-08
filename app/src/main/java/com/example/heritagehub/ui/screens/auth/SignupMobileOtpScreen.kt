@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.heritagehub.viewmodel.AuthViewModel

@Composable
fun SignupMobileOtpScreen(
    viewModel: AuthViewModel,
    username: String,
    email: String,
    role: String = "user",
    onSignupSuccess: () -> Unit,
    onBackToMethodSelection: () -> Unit
) {
    val phoneNumber = remember { mutableStateOf("") }
    val otp = remember { mutableStateOf("") }
    val showOtpInput = remember { mutableStateOf(false) }
    val isLoading = viewModel.isLoading.value
    val errorMessage = viewModel.error.value
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
                text = if (showOtpInput.value) "Enter Verification Code" else "Verify Your Mobile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = if (showOtpInput.value)
                    "We sent a 6-digit code to your mobile"
                else
                    "Enter your mobile number to receive an OTP",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 24.dp)
            )

            SignupOtpInfoRow(label = "Username", value = username)
            SignupOtpInfoRow(label = "Email", value = email)

            if (!showOtpInput.value) {
                // Phone input step
                OutlinedTextField(
                    value = phoneNumber.value,
                    onValueChange = { phoneNumber.value = it },
                    label = { Text("Mobile Number") },
                    placeholder = { Text("+1 (555) 000-0000") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    enabled = !isLoading,
                    singleLine = true,
                    prefix = { Text("+") }
                )

                Button(
                    onClick = {
                        if (phoneNumber.value.isNotEmpty()) {
                            val activity = context as? Activity
                            if (activity != null) {
                                viewModel.requestPhoneOtp(
                                    phoneNum = "+${phoneNumber.value.filter { it.isDigit() }}",
                                    activity = activity,
                                    onCodeSent = {
                                        showOtpInput.value = true
                                    }
                                )
                            }
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
                // OTP input step
                OutlinedTextField(
                    value = otp.value,
                    onValueChange = { otp.value = it.take(6) },
                    label = { Text("Verification Code") },
                    placeholder = { Text("000000") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    enabled = !isLoading,
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (otp.value.length == 6) {
                            viewModel.verifyPhoneOtp(
                                otp = otp.value,
                                username = username,
                                email = email,
                                role = role,
                                onSuccess = onSignupSuccess
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
                        Text("Verify & Complete Signup")
                    }
                }

                TextButton(
                    onClick = { showOtpInput.value = false },
                    enabled = !isLoading
                ) {
                    Text("Edit phone number", fontSize = 12.sp)
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            TextButton(onClick = onBackToMethodSelection, enabled = !isLoading, modifier = Modifier.padding(top = 24.dp)) {
                Text("Choose different method", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SignupOtpInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
