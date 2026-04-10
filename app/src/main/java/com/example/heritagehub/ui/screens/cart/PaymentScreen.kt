package com.example.heritagehub.ui.screens.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heritagehub.model.CheckoutPreferences
import com.example.heritagehub.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val cartViewModel: CartViewModel = viewModel()
    val savedPreferences = cartViewModel.checkoutPreferences.value

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address1 by remember { mutableStateOf("") }
    var address2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash on Delivery") }

    LaunchedEffect(savedPreferences) {
        fullName = savedPreferences.fullName
        phone = savedPreferences.phoneNumber
        address1 = savedPreferences.addressLine1
        address2 = savedPreferences.addressLine2
        city = savedPreferences.city
        state = savedPreferences.state
        pincode = savedPreferences.pincode
        paymentMethod = savedPreferences.paymentMethod
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Address & Payment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Delivery Address", fontWeight = FontWeight.Bold)

            OutlinedTextField(fullName, { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone, { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(address1, { address1 = it }, label = { Text("Address Line 1") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(address2, { address2 = it }, label = { Text("Address Line 2 (Optional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(city, { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state, { state = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(pincode, { pincode = it }, label = { Text("Pincode") }, modifier = Modifier.fillMaxWidth())

            Text("Payment Method", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            PaymentOptionRow(
                title = "Cash on Delivery",
                selected = paymentMethod == "Cash on Delivery",
                onSelect = { paymentMethod = "Cash on Delivery" }
            )
            PaymentOptionRow(
                title = "UPI",
                selected = paymentMethod == "UPI",
                onSelect = { paymentMethod = "UPI" }
            )
            PaymentOptionRow(
                title = "Card",
                selected = paymentMethod == "Card",
                onSelect = { paymentMethod = "Card" }
            )

            Button(
                onClick = {
                    val preferences = CheckoutPreferences(
                        fullName = fullName,
                        phoneNumber = phone,
                        addressLine1 = address1,
                        addressLine2 = address2,
                        city = city,
                        state = state,
                        pincode = pincode,
                        paymentMethod = paymentMethod
                    )
                    cartViewModel.saveCheckoutPreferences(preferences) {
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = fullName.isNotBlank() && phone.isNotBlank() && address1.isNotBlank() && city.isNotBlank() && state.isNotBlank() && pincode.isNotBlank()
            ) {
                Text("Save Details")
            }
        }
    }
}

@Composable
private fun PaymentOptionRow(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(title)
    }
}

