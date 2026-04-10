package com.example.heritagehub.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heritagehub.data.CartRepository
import com.example.heritagehub.model.Artwork
import com.example.heritagehub.model.CartItem
import com.example.heritagehub.model.CartSummary
import com.example.heritagehub.model.CheckoutPreferences
import com.example.heritagehub.model.Order
import kotlinx.coroutines.launch

class CartViewModel(
    private val repository: CartRepository = CartRepository()
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
}



