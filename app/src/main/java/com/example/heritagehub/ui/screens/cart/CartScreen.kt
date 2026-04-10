package com.example.heritagehub.ui.screens.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heritagehub.model.CartItem
import com.example.heritagehub.util.PriceUtils
import com.example.heritagehub.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    onContinueShopping: () -> Unit
) {
    val cartViewModel: CartViewModel = viewModel()
    val items = cartViewModel.items.value
    val summary = cartViewModel.summary.value
    val isLoading = cartViewModel.isLoading.value
    val cartMessage = cartViewModel.message.value
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        cartViewModel.refreshCart()
    }

    LaunchedEffect(cartMessage) {
        if (!cartMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(cartMessage)
            cartViewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Cart") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        IconButton(onClick = { cartViewModel.clearCart() }) {
                            Icon(Icons.Default.RemoveShoppingCart, contentDescription = "Clear Cart")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PriceLine("Subtotal", PriceUtils.formatCurrency(summary.subtotal))
                        PriceLine("Delivery", PriceUtils.formatCurrency(summary.deliveryFee))
                        PriceLine("Tax", PriceUtils.formatCurrency(summary.tax))
                        Divider()
                        PriceLine("Total", PriceUtils.formatCurrency(summary.total), true)
                        Button(
                            onClick = onCheckout,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Proceed to Checkout (${summary.itemCount} items)")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Loading cart...")
                }
            }

            items.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your cart is empty")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onContinueShopping) {
                        Text("Continue Shopping")
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.artworkId }) { item ->
                        CartItemCard(
                            item = item,
                            onIncrement = { cartViewModel.increment(item) },
                            onDecrement = { cartViewModel.decrement(item) },
                            onRemove = { cartViewModel.remove(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(item.title, fontWeight = FontWeight.SemiBold)
            Text("by ${item.artistName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(item.priceDisplay.ifBlank { PriceUtils.formatCurrency(item.unitPrice) })
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onDecrement) { Text("-") }
                    Text(
                        text = "  ${item.quantity}  ",
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(onClick = onIncrement) { Text("+") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(PriceUtils.formatCurrency(item.lineTotal), fontWeight = FontWeight.Bold)
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceLine(label: String, value: String, isEmphasis: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isEmphasis) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (isEmphasis) FontWeight.Bold else FontWeight.Medium)
    }
}


