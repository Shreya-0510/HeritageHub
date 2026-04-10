package com.example.heritagehub

import com.example.heritagehub.model.CartItem
import com.example.heritagehub.util.CartPricing
import com.example.heritagehub.util.PriceUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class CartPricingTest {

    @Test
    fun `calculateSummary computes totals with tax and delivery`() {
        val items = listOf(
            CartItem(artworkId = "a1", unitPrice = 400.0, quantity = 2),
            CartItem(artworkId = "a2", unitPrice = 200.0, quantity = 1)
        )

        val summary = CartPricing.calculateSummary(items)

        assertEquals(1000.0, summary.subtotal, 0.0001)
        assertEquals(79.0, summary.deliveryFee, 0.0001)
        assertEquals(50.0, summary.tax, 0.0001)
        assertEquals(1129.0, summary.total, 0.0001)
        assertEquals(3, summary.itemCount)
    }

    @Test
    fun `parsePriceToDouble strips currency and commas`() {
        val value = PriceUtils.parsePriceToDouble("Rs 1,299.50")
        assertEquals(1299.5, value, 0.0001)
    }
}

