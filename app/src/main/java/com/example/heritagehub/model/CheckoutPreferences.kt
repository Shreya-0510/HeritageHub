package com.example.heritagehub.model

data class CheckoutPreferences(
    val fullName: String = "",
    val phoneNumber: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val paymentMethod: String = "Cash on Delivery"
) {
    fun isValid(): Boolean {
        return fullName.isNotBlank() &&
            phoneNumber.isNotBlank() &&
            addressLine1.isNotBlank() &&
            city.isNotBlank() &&
            state.isNotBlank() &&
            pincode.isNotBlank()
    }

    fun formattedAddress(): String {
        return listOf(addressLine1, addressLine2, city, state, pincode)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}

