package com.example.heritagehub.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.example.heritagehub.data.CartRepository
import com.example.heritagehub.model.Artwork
import com.example.heritagehub.model.CartItem
import com.example.heritagehub.model.CartSummary
import com.example.heritagehub.model.CheckoutPreferences
import com.example.heritagehub.model.Order
import com.example.heritagehub.util.LocationUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class CartViewModel(
    private val repository: CartRepository = CartRepository(),
    private val appContext: Context? = null
) : ViewModel() {

    private val _items = mutableStateOf<List<CartItem>>(emptyList())
    val items: State<List<CartItem>> = _items

    private val _summary = mutableStateOf(CartSummary())
    val summary: State<CartSummary> = _summary

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isSubmittingOrder = mutableStateOf(false)
    val isSubmittingOrder: State<Boolean> = _isSubmittingOrder

    private val _message = mutableStateOf<String?>(null)
    val message: State<String?> = _message

    private val _checkoutPreferences = mutableStateOf(CheckoutPreferences())
    val checkoutPreferences: State<CheckoutPreferences> = _checkoutPreferences

    private val _orders = mutableStateOf<List<Order>>(emptyList())
    val orders: State<List<Order>> = _orders

    private val _isLoadingOrders = mutableStateOf(false)
    val isLoadingOrders: State<Boolean> = _isLoadingOrders

    private val _isFetchingLocation = mutableStateOf(false)
    val isFetchingLocation: State<Boolean> = _isFetchingLocation
    private val _locationError = mutableStateOf<String?>(null)
    val locationError: State<String?> = _locationError

    init {
        refreshCart()
        refreshCheckoutPreferences()
    }

    fun refreshCart() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cartItems = repository.getCartItems()
                _items.value = cartItems
                _summary.value = repository.calculateSummary(cartItems)
            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to load cart"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToCart(
        artwork: Artwork,
        quantity: Int = 1,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                repository.addToCart(artwork, quantity)
                _message.value = "Added to cart"
                refreshCart()
                onResult?.invoke(true)
            } catch (e: Exception) {
                _message.value = e.message ?: "Unable to add item"
                onResult?.invoke(false)
            }
        }
    }

    fun increment(item: CartItem) {
        updateQuantity(item, item.quantity + 1)
    }

    fun decrement(item: CartItem) {
        updateQuantity(item, item.quantity - 1)
    }

    fun updateQuantity(item: CartItem, newQuantity: Int) {
        viewModelScope.launch {
            try {
                repository.updateQuantity(item.artworkId, newQuantity)
                refreshCart()
            } catch (e: Exception) {
                _message.value = e.message ?: "Unable to update quantity"
            }
        }
    }

    fun remove(item: CartItem) {
        viewModelScope.launch {
            try {
                repository.removeFromCart(item.artworkId)
                _message.value = "Removed from cart"
                refreshCart()
            } catch (e: Exception) {
                _message.value = e.message ?: "Unable to remove item"
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                repository.clearCart()
                _message.value = "Cart cleared"
                refreshCart()
            } catch (e: Exception) {
                _message.value = e.message ?: "Unable to clear cart"
            }
        }
    }

    fun placeOrder(onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isSubmittingOrder.value = true
            try {
                val orderId = repository.createOrderFromCart(
                    items = _items.value,
                    summary = _summary.value,
                    preferences = _checkoutPreferences.value
                )
                _message.value = "Order placed successfully"
                refreshCart()
                refreshOrders()
                onSuccess(orderId)
            } catch (e: Exception) {
                _message.value = e.message ?: "Unable to place order"
            } finally {
                _isSubmittingOrder.value = false
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun refreshCheckoutPreferences() {
        viewModelScope.launch {
            try {
                _checkoutPreferences.value = repository.getCheckoutPreferences()
            } catch (e: Exception) {
                _message.value = e.message ?: "Unable to load checkout details"
            }
        }
    }

    fun saveCheckoutPreferences(
        preferences: CheckoutPreferences,
        onSaved: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                repository.saveCheckoutPreferences(preferences)
                _checkoutPreferences.value = preferences
                _message.value = "Address and payment details saved"
                onSaved?.invoke()
            } catch (e: Exception) {
                _message.value = e.message ?: "Unable to save checkout details"
            }
        }
    }

    fun refreshOrders() {
        viewModelScope.launch {
            _isLoadingOrders.value = true
            try {
                _orders.value = repository.getOrders()
            } catch (e: Exception) {
                _message.value = e.message ?: "Unable to load orders"
            } finally {
                _isLoadingOrders.value = false
            }
        }
    }

    suspend fun fetchAndSetCurrentAddress(context: Context) {
        _isFetchingLocation.value = true
        _locationError.value = null
        try {
            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
            val location: Location? = fusedLocationClient.lastLocation.await()
            if (location != null) {
                val geocoded = LocationUtils.reverseGeocode(context, location)
                if (geocoded != null) {
                    val currentPrefs = _checkoutPreferences.value.copy(
                        addressLine1 = geocoded.addressLine1,
                        addressLine2 = geocoded.addressLine2,
                        city = geocoded.city,
                        state = geocoded.state,
                        pincode = geocoded.pincode
                    )
                    saveCheckoutPreferences(currentPrefs)
                } else {
                    _locationError.value = "Unable to fetch address from location."
                }
            } else {
                _locationError.value = "Unable to fetch location."
            }
        } catch (e: SecurityException) {
            _locationError.value = "Location permission denied."
        } catch (e: Exception) {
            _locationError.value = "Failed to fetch location."
        } finally {
            _isFetchingLocation.value = false
        }
    }
}
