package com.example.heritagehub.ui.screens.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heritagehub.util.PriceUtils
import com.example.heritagehub.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val cartViewModel: CartViewModel = viewModel()
    val items = cartViewModel.items.value
    val summary = cartViewModel.summary.value
    val isSubmitting = cartViewModel.isSubmittingOrder.value
    val preferences = cartViewModel.checkoutPreferences.value
    val message = cartViewModel.message.value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        cartViewModel.refreshCart()
        cartViewModel.refreshCheckoutPreferences()
    }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            cartViewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Your cart is empty")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Delivery Address", fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (preferences.isValid()) {
                        Text(preferences.fullName, fontWeight = FontWeight.SemiBold)
                        Text(preferences.formattedAddress())
                        Text(preferences.phoneNumber)
                        Text("Payment: ${preferences.paymentMethod}")
                    } else {
                        Text("No delivery address added yet")
                    }
                    OutlinedButton(onClick = onNavigateToPayment) {
                        Text(if (preferences.isValid()) "Change Address / Payment" else "Add Address / Payment")
                    }
                }
            }

            Text("Items", fontWeight = FontWeight.Bold)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.artworkId }) { item ->
                    Card {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.SemiBold)
                                Text("Qty: ${item.quantity}")
                            }
                            Text(PriceUtils.formatCurrency(item.lineTotal), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CheckoutPriceLine("Subtotal", PriceUtils.formatCurrency(summary.subtotal))
                    CheckoutPriceLine("Delivery", PriceUtils.formatCurrency(summary.deliveryFee))
                    CheckoutPriceLine("Tax", PriceUtils.formatCurrency(summary.tax))
                    Divider()
                    CheckoutPriceLine("Total", PriceUtils.formatCurrency(summary.total), true)
                }
            }

            Button(
                onClick = {
                    cartViewModel.placeOrder {
                        onOrderPlaced()
                    }
                },
                enabled = !isSubmitting && preferences.isValid(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSubmitting) "Placing Order..." else "Place Order")
            }
        }
    }
}

@Composable
private fun CheckoutPriceLine(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}



