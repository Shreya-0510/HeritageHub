package com.example.heritagehub.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heritagehub.model.Order
import com.example.heritagehub.util.PriceUtils
import com.example.heritagehub.viewmodel.CartViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onBack: () -> Unit
) {
    val cartViewModel: CartViewModel = viewModel()
    val orders = cartViewModel.orders.value
    val isLoading = cartViewModel.isLoadingOrders.value

    LaunchedEffect(Unit) {
        cartViewModel.refreshOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
                    CircularProgressIndicator()
                }
            }

            orders.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No active orders yet")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(order)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order #${order.id.take(8)}", fontWeight = FontWeight.Bold)
                Text(order.status.replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.primary)
            }
            Text("Item: ${order.firstItemTitle.ifBlank { "Artwork" }}")
            Text("Items: ${order.itemCount}")
            Text("Total: ${PriceUtils.formatCurrency(order.total)}", fontWeight = FontWeight.Bold)
            Text("Delivering to: ${order.deliveringTo.ifBlank { "Address not set" }}")
            Text("Payment: ${order.paymentMethod.ifBlank { "Not specified" }}")
            if (order.createdAt > 0L) {
                val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(order.createdAt))
                Text("Placed on: $date", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

