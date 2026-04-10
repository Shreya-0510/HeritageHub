package com.example.heritagehub.util

import com.example.heritagehub.model.CartItem
import com.example.heritagehub.model.CartSummary

object CartPricing {
    fun calculateSummary(items: List<CartItem>): CartSummary {
        val subtotal = items.sumOf { it.lineTotal }
        val itemCount = items.sumOf { it.quantity }
        val deliveryFee = if (subtotal in 0.01..1499.99) 79.0 else 0.0
        val tax = subtotal * 0.05
        val total = subtotal + deliveryFee + tax

        return CartSummary(
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            tax = tax,
            total = total,
            itemCount = itemCount
        )
    }
}

