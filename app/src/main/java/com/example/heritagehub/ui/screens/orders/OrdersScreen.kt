package com.example.heritagehub.ui.screens.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heritagehub.model.Order
import com.example.heritagehub.model.CustomizationRequest
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
    val requests = cartViewModel.userRequests.value
    val isLoading = cartViewModel.isLoadingOrders.value

    LaunchedEffect(Unit) {
        cartViewModel.refreshOrders()
        cartViewModel.refreshUserRequests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Activity") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customization Requests Section
                if (requests.isNotEmpty()) {
                    item {
                        Text("Customization Requests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(items = requests, key = { it.id }) { request ->
                        UserRequestCard(request = request)
                    }
                }

                // Standard Orders Section
                item {
                    Text("Purchase History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (orders.isEmpty()) {
                    item {
                        Text("No active orders yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(items = orders, key = { it.id }) { order ->
                        OrderCard(order)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRequestCard(request: CustomizationRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Request to ${request.artistName}", fontWeight = FontWeight.Bold)
                StatusBadge(status = request.status)
            }
            Text(text = request.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Budget: ${request.budget}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                
                if (request.status.lowercase() == "accepted") {
                    Button(
                        onClick = { /* Add to cart / Purchase flow */ },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("Complete Purchase", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "pending" -> Color(0xFFFFA000)
        "accepted" -> Color(0xFF4CAF50)
        "declined" -> Color(0xFFE91E63)
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
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
