package com.example.heritagehub.util

import java.util.Locale

object PriceUtils {
    fun parsePriceToDouble(priceText: String): Double {
        val normalized = priceText
            .replace(",", "")
            .replace(Regex("[^0-9.]"), "")
        return normalized.toDoubleOrNull() ?: 0.0
    }

    fun formatCurrency(value: Double): String {
        return String.format(Locale.US, "Rs %.2f", value)
    }
}

